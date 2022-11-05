package me.luizotavio.minecraft.factory;

import com.mattmalec.pterodactyl4j.DataType;
import com.mattmalec.pterodactyl4j.EnvironmentValue;
import com.mattmalec.pterodactyl4j.application.entities.*;
import com.mattmalec.pterodactyl4j.exceptions.NotFoundException;
import me.luizotavio.minecraft.comparator.NodeComparator;
import me.luizotavio.minecraft.exception.*;
import me.luizotavio.minecraft.server.PteroServer;
import me.luizotavio.minecraft.server.PteroServerImpl;
import me.luizotavio.minecraft.user.PteroUser;
import me.luizotavio.minecraft.user.PteroUserImpl;
import me.luizotavio.minecraft.util.Try;
import me.luizotavio.minecraft.util.Users;
import me.luizotavio.minecraft.vo.PteroBridgeVO;
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

    public static final NodeComparator NODE_COMPARATOR = new NodeComparator();

    private final PteroBridgeVO bridge;

    public PteroFactoryImpl(@NotNull PteroBridgeVO bridge) {
        this.bridge = bridge;
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
        return CompletableFuture.supplyAsync(() -> {
            boolean exists = false;
            try {
                exists = bridge.getApplication()
                    .retrieveServersByName(name, true)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute()
                    .size() > 0;
            } catch (Exception ignored) {}

            if (exists) {
                throw new RuntimeException(new ServerAlreadyExistsException(name));
            }

            return false;
        }, bridge.getWorker()).thenApply(exists -> {
            if (exists) {
                throw new RuntimeException(new ServerAlreadyExistsException(name));
            }

            // Let's find an node with enough resources
            Node targetNode = bridge.getApplication()
                .retrieveNodes()
                .cache(false)
                .takeWhileAsync(node -> !node.hasMaintanceMode())
                .thenApply(collection -> {
                    // Look for the most available node
                    return collection.stream()
                        .min(NODE_COMPARATOR);
                }).thenApply(optional -> {
                    if (optional.isEmpty()) {
                        throw new RuntimeException(new InsufficientResourcesException());
                    }

                    return optional.get();
                }).join();

            // Let's find an allocation available
            ApplicationAllocation allocation = targetNode.retrieveAllocations()
                .cache(false)
                .takeWhileAsync(allocation1 -> !allocation1.isAssigned())
                .thenApply(collection -> {
                    // Let's get random allocation
                    return collection.stream()
                        .findAny();
                }).thenApply(optional -> {
                    if (optional.isEmpty()) {
                        throw new RuntimeException(new InsufficientAllocationsException());
                    }

                    return optional.get();
                }).join();

            Location location = targetNode.retrieveLocation()
                .execute();

            Map<String, EnvironmentValue<?>> envMap = new HashMap<>();

            envMap.put("SERVER_NAME", EnvironmentValue.of(name));
            envMap.put("SERVER_PORT", EnvironmentValue.of(allocation.getPort()));
            envMap.put("SERVER_IP", EnvironmentValue.of(allocation.getIP()));
            envMap.put("SERVER_JARFILE", EnvironmentValue.of("server.jar"));

            ApplicationEgg targetEgg = bridge.getApplication()
                .retrieveEggs()
                .execute()
                .stream()
                .filter(applicationEgg -> applicationEgg.getName().equalsIgnoreCase(egg))
                .findAny()
                .orElseThrow(() -> new RuntimeException(new EggDoesntExistException(egg)));

            Try<Optional<ApplicationUser>> catching = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveUsersByEmail(owner.getEmail(), true)
                    .execute()
                    .stream()
                    .findAny();
            });

            catching.catching(NotFoundException.class, unused -> {
                throw new RuntimeException(new UserDoesntExistException(owner.getEmail()));
            });

            ApplicationUser applicationUser = catching.unwrap()
                .orElseThrow(() -> new RuntimeException(new UserDoesntExistException(owner.getEmail())));

            ApplicationServer applicationServer = bridge.getApplication()
                .createServer()
                .setName(name)
                .setOwner(applicationUser)
                .setDescription("Dedicated server for " + owner.getName())
                .setEgg(targetEgg)
                .setLocation(location)
                .setAllocation(allocation)
                .setBackups(0)
                .setDatabases(0)
                .setCPU(cpu)
                .setDockerImage(dockerImage)
                .setMemory(memory, DataType.MB)
                .setStartupCommand(startupCommand)
                .setEnvironment(envMap)
                .startOnCompletion(false)
                .setDisk(disk, DataType.MB)
                .execute(true);

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
        return CompletableFuture.supplyAsync(() -> {
            boolean exists = false;
            try {
                exists = bridge.getApplication()
                    .retrieveUsersByUsername(username, true)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute()
                    .size() > 0;
            } catch (Exception ignored) {}

            if (exists) {
                throw new RuntimeException(new UserAlreadyExistsException());
            }

            return false;
        }, bridge.getWorker()).thenApply(exists -> {
            if (exists) {
                throw new RuntimeException(new UserAlreadyExistsException());
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
