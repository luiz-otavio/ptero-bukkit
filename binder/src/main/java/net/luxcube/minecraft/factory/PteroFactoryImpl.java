package net.luxcube.minecraft.factory;

import com.mattmalec.pterodactyl4j.DataType;
import com.mattmalec.pterodactyl4j.EnvironmentValue;
import com.mattmalec.pterodactyl4j.application.entities.ApplicationEgg;
import com.mattmalec.pterodactyl4j.application.entities.ApplicationServer;
import com.mattmalec.pterodactyl4j.application.entities.ApplicationUser;
import com.mattmalec.pterodactyl4j.application.entities.Node;
import com.mattmalec.pterodactyl4j.client.entities.Account;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.entities.Allocation;
import com.mattmalec.pterodactyl4j.exceptions.PteroException;
import net.luxcube.minecraft.comparator.NodeComparator;
import net.luxcube.minecraft.exception.EggDoesntExistException;
import net.luxcube.minecraft.exception.InsufficientResourcesException;
import net.luxcube.minecraft.exception.ServerAlreadyExistsException;
import net.luxcube.minecraft.exception.UserAlreadyExistsException;
import net.luxcube.minecraft.listener.ServerListener;
import net.luxcube.minecraft.logger.PteroLogger;
import net.luxcube.minecraft.server.PteroServer;
import net.luxcube.minecraft.server.PteroServerImpl;
import net.luxcube.minecraft.user.PteroUser;
import net.luxcube.minecraft.user.PteroUserImpl;
import net.luxcube.minecraft.util.LuxcubeThrowner;
import net.luxcube.minecraft.util.Try;
import net.luxcube.minecraft.util.Users;
import net.luxcube.minecraft.vo.PteroBridgeVO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class PteroFactoryImpl implements PteroFactory {

    private static final LuxcubeThrowner ALREADY_EXISTS_THROWN = new LuxcubeThrowner<>(UserAlreadyExistsException::new);

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
                .stream()
                .min(NODE_COMPARATOR)
                .orElseThrow(InsufficientResourcesException::new);

            Map<String, EnvironmentValue<?>> envMap = new HashMap<>();

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
                .setDescription(owner.getName() + "'s server")
                .setEgg(targetEgg)
                .setAllocations(1)
                .setCPU(cpu)
                .setDockerImage(dockerImage)
                .setMemory(memory, DataType.MB)
                .setStartupCommand(startupCommand)
                .setEnvironment(envMap)
                .setDatabases(1)
                .setDisk(disk, DataType.MB)
                .execute();

            ClientServer server = bridge.getClient()
                .retrieveServerByIdentifier(applicationServer.getIdentifier())
                .execute();

            if (server.isInstalling()) {
                server.getWebSocketBuilder()
                    .addEventListeners(new ServerListener(bridge, owner.getEmail()))
                    .build();
            }

            // Remove unnecessary request
            Allocation allocation = server.getPrimaryAllocation();

            return new PteroServerImpl(
                bridge,
                applicationServer.getIdentifier(),
                allocation.getFullAddress(),
                targetNode.getName(),
                name,
                applicationServer.getUUID(),
                applicationServer.getId()
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

            Try<ApplicationUser> catching = Try.catching(() -> {
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
            });

            catching.catching(PteroException.class, ALREADY_EXISTS_THROWN);

            return catching.unwrap();
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
