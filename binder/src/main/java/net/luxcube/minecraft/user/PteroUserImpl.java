package net.luxcube.minecraft.user;

import com.mattmalec.pterodactyl4j.application.entities.ApplicationUser;
import com.mattmalec.pterodactyl4j.exceptions.NotFoundException;
import net.luxcube.minecraft.exception.UserDoesntExistException;
import net.luxcube.minecraft.logger.PteroLogger;
import net.luxcube.minecraft.server.PteroServer;
import net.luxcube.minecraft.server.PteroServerImpl;
import net.luxcube.minecraft.util.Pair;
import net.luxcube.minecraft.util.Servers;
import net.luxcube.minecraft.util.Try;
import net.luxcube.minecraft.vo.PteroBridgeVO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class PteroUserImpl implements PteroUser {

    private final PteroBridgeVO bridge;

    private final String id;
    private final String name;
    private final String email;

    private final UUID uniqueId;
    private final String password;

    public PteroUserImpl(
        @NotNull PteroBridgeVO bridge,
        @NotNull String id,
        @NotNull String name,
        @NotNull String email,
        @Nullable UUID uniqueId,
        @Nullable String password
    ) {
        this.bridge = bridge;
        this.id = id;
        this.name = name;
        this.email = email;
        this.uniqueId = uniqueId;
        this.password = password;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @Nullable UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getEmail() {
        return email;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public @NotNull CompletableFuture<List<PteroServer>> getServers() {
        PteroLogger.debug("Searching servers by user: %s", name);

        return CompletableFuture.supplyAsync(() -> {
            return bridge.getApplication()
                .retrieveServers()
                .all()
                .execute();
        }, bridge.getWorker()).thenApply(servers -> {
            if (servers.isEmpty()) {
                return Collections.emptyList();
            }

            return servers.stream()
                .filter(server -> server.getOwnerId().equals(id))
                .map(server -> {
                    Pair<String, String> addressAndNode = Servers.getAddressAndNode(server);

                    return new PteroServerImpl(
                        bridge,
                        server.getIdentifier(),
                        server.getId(),
                        addressAndNode.first(),
                        addressAndNode.second(),
                        server.getName(),
                        server.getUUID()
                    );
                }).collect(Collectors.toUnmodifiableList());
        });
    }

    @Override
    public CompletableFuture<Void> setName(@NotNull String name) {
        PteroLogger.debug("Setting user name: %s", name);

        return CompletableFuture.runAsync(() -> {
            Try<Optional<ApplicationUser>> catching = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveUsersByEmail(email, true)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute()
                    .stream()
                    .findFirst();
            });

            catching.catching(NotFoundException.class, e -> {
                throw new UserDoesntExistException(this.name);
            });

            catching.unwrap()
                .orElseThrow(() -> new UserDoesntExistException(this.name))
                .edit()
                .setUserName(name)
                .execute(true);
        }, bridge.getWorker());
    }

    @Override
    public CompletableFuture<Void> setEmail(@NotNull String email) {
        PteroLogger.debug("Setting user email: %s", email);

        return CompletableFuture.runAsync(() -> {
            Try<Optional<ApplicationUser>> catching = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveUsersByEmail(email, true)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute()
                    .stream()
                    .findFirst();
            });

            catching.catching(NotFoundException.class, e -> {
                throw new UserDoesntExistException(this.name);
            });

            catching.unwrap()
                .orElseThrow(() -> new UserDoesntExistException(this.name))
                .edit()
                .setEmail(email)
                .execute(true);
        }, bridge.getWorker());
    }

    @Override
    public CompletableFuture<Void> setPassword(@NotNull String password) {
        PteroLogger.debug("Setting user password: %s", password);

        return CompletableFuture.runAsync(() -> {
            Try<Optional<ApplicationUser>> catching = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveUsersByEmail(email, true)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute()
                    .stream()
                    .findFirst();
            });

            catching.catching(NotFoundException.class, e -> {
                throw new UserDoesntExistException(this.name);
            });

            catching.unwrap()
                .orElseThrow(() -> new UserDoesntExistException(this.name))
                .edit()
                .setPassword(password)
                .execute(true);
        }, bridge.getWorker());
    }
}
