package net.luxcube.minecraft.listener;

import com.mattmalec.pterodactyl4j.Permission;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.client.entities.Schedule;
import com.mattmalec.pterodactyl4j.client.entities.impl.CronImpl;
import com.mattmalec.pterodactyl4j.client.managers.ScheduleTaskManager;
import com.mattmalec.pterodactyl4j.client.ws.events.install.InstallCompletedEvent;
import com.mattmalec.pterodactyl4j.client.ws.hooks.ClientSocketListenerAdapter;
import net.luxcube.minecraft.logger.PteroLogger;
import net.luxcube.minecraft.vo.PteroBridgeVO;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * @author Luiz O. F. Corrêa
 * @since 01/12/2022
 **/
public class ServerListener extends ClientSocketListenerAdapter {

    public static final Permission[] USER_PERMISSIONS = {
        Permission.FILE_SFTP,
        Permission.CONTROL_CONSOLE, Permission.CONTROL_RESTART, Permission.CONTROL_START, Permission.CONTROL_STOP,
        Permission.USER_CREATE, Permission.USER_READ, Permission.USER_DELETE, Permission.USER_UPDATE,
        Permission.DATABASE_CREATE, Permission.DATABASE_READ, Permission.DATABASE_DELETE, Permission.DATABASE_UPDATE, Permission.DATABASE_VIEW_PASSWORD,
        Permission.BACKUP_DOWNLOAD, Permission.BACKUP_READ, Permission.BACKUP_RESTORE
    };

    private final PteroBridgeVO bridge;
    private final String email;

    public ServerListener(@NotNull PteroBridgeVO bridge, @NotNull String email) {
        this.bridge = bridge;
        this.email = email;
    }

    @Override
    public void onInstallCompleted(InstallCompletedEvent event) {
        ClientServer clientServer = event.getServer();

        bridge.getApplication()
            .retrieveServerById(clientServer.getInternalId())
            .executeAsync(applicationServer -> {

                boolean exists = clientServer.getSubusers()
                    .stream()
                    .anyMatch(subuser -> subuser.getEmail().equals(email));

                if (exists) {
                    PteroLogger.debug("User %s is already a subuser", email);
                    return;
                }

                clientServer.getScheduleManager()
                    .createSchedule()
                    .setActive(true)
                    .setCron(CronImpl.ofExpression("0 0 * * MON"))
                    .setName("Backup")
                    .setWhenServerIsOnline(true)
                        .executeAsync(schedule -> {
                            ScheduleTaskManager scheduleTaskManager = schedule.getTaskManager();

                            scheduleTaskManager.createTask()
                                .setAction(Schedule.ScheduleTask.ScheduleAction.BACKUP)
                                .setContinueOnFailure(false)
                                .executeAsync();
                        });

                clientServer.getSubuserManager()
                    .createUser()
                    .setEmail(email)
                    .setPermissions(USER_PERMISSIONS)
                    .timeout(10, TimeUnit.SECONDS)
                    .executeAsync(success -> {
                        PteroLogger.debug("User %s was added to server %s", email, clientServer.getIdentifier());
                    }, throwable -> {
                        PteroLogger.severe(
                            "Failed to add the user " + email + " to the server: " + clientServer.getIdentifier(),
                            throwable
                        );
                    });
            }, throwable -> {
                PteroLogger.severe("Failed to retrieve server by identifier: " + clientServer.getIdentifier(), throwable);
            });

        clientServer.getWebSocketBuilder()
            .removeEventListeners(this);
    }
}
