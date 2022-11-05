package net.luxcube.minecraft.factory;

import com.mattmalec.pterodactyl4j.DataType;
import com.mattmalec.pterodactyl4j.EnvironmentValue;
import com.mattmalec.pterodactyl4j.Permission;
import com.mattmalec.pterodactyl4j.application.entities.*;
import com.mattmalec.pterodactyl4j.client.entities.Account;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.entities.Allocation;
import com.mattmalec.pterodactyl4j.exceptions.NotFoundException;
import net.luxcube.minecraft.comparator.NodeComparator;
import net.luxcube.minecraft.exception.*;
import net.luxcube.minecraft.logger.PteroLogger;
import net.luxcube.minecraft.server.PteroServer;
import net.luxcube.minecraft.server.PteroServerImpl;
import net.luxcube.minecraft.user.PteroUser;
import net.luxcube.minecraft.user.PteroUserImpl;
import net.luxcube.minecraft.util.Users;
import net.luxcube.minecraft.vo.PteroBridgeVO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class PteroFactoryImpl implements PteroFactory {

    public static final Permission[] USER_PERMISSIONS = {
        Permission.FILE_READ, Permission.FILE_CREATE, Permission.FILE_ARCHIVE, Permission.FILE_READ_CONTENT, Permission.FILE_UPDATE,
        Permission.CONTROL_CONSOLE, Permission.CONTROL_RESTART, Permission.CONTROL_START, Permission.CONTROL_STOP,
        Permission.USER_CREATE, Permission.USER_READ, Permission.USER_DELETE, Permission.USER_UPDATE
    };

    public static final NodeComparator NODE_COMPARATOR = new NodeComparator();

    private final PteroBridgeVO bridge;
    private final ApplicationUser account;

    public PteroFactoryImpl(@NotNull PteroBridgeVO bridge) {
        this.bridge = bridge;

        Account clientAccount = bridge.getClient()
            .retrieveAccount()
            .execute();

        this.account = bridge.getApplication()
            .retrieveUserById(clientAccount.getId())
            .execute();
    }

    @Override
    public CompletableFuture<PteroServer> createServer(
        @NotNull String name,
        @NotNull PteroUser owner,
        @NotNull String egg,
        @NotNull String dockerImage,
        @NotNull String startupCommand,
        int memory,
        int disk,
        int cpu
    ) {
        PteroLogger.debug("Creating server %s for %s", name, owner.getName());

        return CompletableFuture.supplyAsync(() -> {
            boolean exists = false;
            try {
                exists = bridge.getApplication()
                    .retrieveServersByName(name, true)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute()
                    .size() > 0;
            } catch (Exception ignored) {
                throw new ServerAlreadyExistsException(name);
            }

            if (exists) {
                throw new ServerAlreadyExistsException(name);
            }

            return false;
        }, bridge.getWorker()).thenApply(exists -> {
            if (exists) {
                throw new ServerAlreadyExistsException(name);
            }

            // Let's find an node with enough resources
            Node targetNode = bridge.getApplication()
                .retrieveNodes()
                .cache(false)
                .takeWhileAsync(node -> !node.hasMaintanceMode())
                .exceptionally(throwable -> {
                    if (throwable instanceof NotFoundException) {
                        throw new InsufficientResourcesException();
                    }

                    throw new RuntimeException(throwable);
                })
                .thenApply(collection -> {
                    Optional<Node> optional = collection.stream()
                        .min(NODE_COMPARATOR);

                    if (optional.isPresent()) {
                        return optional.get();
                    }

                    throw new InsufficientResourcesException();
                }).join();

            Location location = targetNode.retrieveLocation()
                .execute();

            Map<String, EnvironmentValue<?>> envMap = new HashMap<>();

            envMap.put("SERVER_NAME", EnvironmentValue.of(name));
            envMap.put("SERVER_JARFILE", EnvironmentValue.of("server.jar"));

            ApplicationEgg targetEgg = bridge.getApplication()
                .retrieveEggs()
                .execute()
                .stream()
                .filter(applicationEgg -> applicationEgg.getName().equalsIgnoreCase(egg))
                .findAny()
                .orElseThrow(() -> new EggDoesntExistException(egg));

            ApplicationServer applicationServer = bridge.getApplication()
                .createServer()
                .setName(name)
                .setOwner(account)
                .setDescription("Dedicated server for " + owner.getName())
                .setEgg(targetEgg)
                .setLocation(location)
                .setAllocations(1)
                .setBackups(0)
                .setDatabases(0)
                .setCPU(cpu)
                .setDockerImage(dockerImage)
                .setMemory(memory, DataType.MB)
                .setStartupCommand(startupCommand)
                .setEnvironment(envMap)
                .startOnCompletion(false)
                .setDisk(disk, DataType.MB)
                .execute();

            // Let's wait for the server to be created
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }

            ClientServer clientServer = bridge.getClient()
                .retrieveServerByIdentifier(applicationServer.getIdentifier())
                .execute();

            clientServer.getSubuserManager()
                .createUser()
                .setEmail(owner.getEmail())
                .setPermissions(USER_PERMISSIONS)
                .execute(true);

            Allocation allocation = applicationServer.retrieveDefaultAllocation()
                .execute();

            return new PteroServerImpl(
                bridge,
                applicationServer.getIdentifier(),
                allocation.getFullAddress(),
                targetNode.getName(),
                applicationServer.getUUID()
            );
        });
    }

    @Override
    public CompletableFuture<PteroUser> createUser(
        @NotNull UUID uuid,
        @NotNull String username,
        @NotNull String password,
        @Nullable String email
    ) {
        PteroLogger.debug("Creating user %s", username);

        return CompletableFuture.supplyAsync(() -> {
            boolean exists = false;
            try {
                exists = bridge.getApplication()
                    .retrieveUsersByUsername(username, true)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute()
                    .size() > 0;
            } catch (Exception ignored) {
                throw new UserAlreadyExistsException();
            }

            if (exists) {
                throw new UserAlreadyExistsException();
            }

            return false;
        }, bridge.getWorker()).thenApply(exists -> {
            if (exists) {
                throw new UserAlreadyExistsException();
            }

            String fromShort = Users.fromShort(uuid);

            return bridge.getApplication()
                .getUserManager()
                .createUser()
                .setPassword(password)
                .setEmail(email == null ? String.format("%s@%s", username, "luxcube.net") : email)
                .setUserName(username)
                .setFirstName(fromShort)
                .setLastName("'s Account")
                .timeout(10, TimeUnit.SECONDS)
                .execute(true);
        }).thenApply(applicationUser -> {
            return new PteroUserImpl(
                bridge,
                applicationUser.getId(),
                username,
                applicationUser.getEmail(),
                uuid,
                password
            );
        });
    }
}
