package me.luizotavio.minecraft.repository.user;

import com.mattmalec.pterodactyl4j.application.entities.ApplicationUser;
import com.mattmalec.pterodactyl4j.exceptions.NotFoundException;
import me.luizotavio.minecraft.exception.UserDoesntExistException;
import me.luizotavio.minecraft.user.PteroUser;
import me.luizotavio.minecraft.user.PteroUserImpl;
import me.luizotavio.minecraft.util.Try;
import me.luizotavio.minecraft.util.Users;
import me.luizotavio.minecraft.vo.PteroBridgeVO;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class UserRepositoryImpl implements UserRepository {

    private final PteroBridgeVO bridge;

    public UserRepositoryImpl(@NotNull PteroBridgeVO bridge) {
        this.bridge = bridge;
    }

    @Override
    public CompletableFuture<PteroUser> findUserByUsername(@NotNull String username) {
        return CompletableFuture.supplyAsync(() -> {
            Try<Optional<ApplicationUser>> catching = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveUsersByUsername(username, true)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute()
                    .stream()
                    .findFirst();
            });

            catching.catching(NotFoundException.class, e -> {
                throw new RuntimeException(new UserDoesntExistException(username));
            });

            return catching.unwrap()
                .orElseThrow(() -> new RuntimeException(new UserDoesntExistException(username)));
        }, bridge.getWorker()).thenApply(user -> {
            return new PteroUserImpl(
                bridge,
                user.getId(),
                username,
                user.getEmail(),
                null,
                null
            );
        });
    }

    @Override
    public CompletableFuture<PteroUser> findUserByUUID(@NotNull UUID uuid) {
        String fromShort = Users.fromShort(uuid);

        return bridge.getApplication()
            .retrieveUsers()
            .cache(true)
            .timeout(10, TimeUnit.SECONDS)
            .takeWhileAsync(1, user -> user.getFirstName().equals(fromShort))
            .thenApply(users -> {
                if (users.isEmpty()) {
                    throw new RuntimeException(new UserDoesntExistException(uuid.toString()));
                }

                return users.stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(new UserDoesntExistException(uuid.toString())));
            }).thenApply(user -> {
                return new PteroUserImpl(
                    bridge,
                    user.getId(),
                    user.getUserName(),
                    user.getEmail(),
                    null,
                    null
                );
            });
    }

    @Override
    public CompletableFuture<PteroUser> findUserByEmail(@NotNull String email) {
        return CompletableFuture.supplyAsync(() -> {
            Try<Optional<ApplicationUser>> applicationUser = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveUsersByEmail(email, true)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute()
                    .stream()
                    .findFirst();
            });

            applicationUser.catching(NotFoundException.class, e -> {
                throw new RuntimeException(new UserDoesntExistException(email));
            });

            return applicationUser.unwrap()
                .orElseThrow(() -> new RuntimeException(new UserDoesntExistException(email)));
        }, bridge.getWorker()).thenApply(user -> {
            return new PteroUserImpl(
                bridge,
                user.getId(),
                user.getUserName(),
                email,
                null,
                null
            );
        });
    }

    @Override
    public CompletableFuture<PteroUser> findUserBySnowflake(@NotNull String snowflake) {
        return CompletableFuture.supplyAsync(() -> {
            Try<ApplicationUser> catching = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveUserById(snowflake)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute();
            });

            catching.catching(NotFoundException.class, e -> {
                throw new RuntimeException(new UserDoesntExistException(snowflake));
            });

            return catching.unwrap();
        }, bridge.getWorker()).thenApply(user -> {
            return new PteroUserImpl(
                bridge,
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                null,
                null
            );
        });
    }

    @Override
    public CompletableFuture<PteroUser> deleteUser(@NotNull PteroUser user) {
        return CompletableFuture.runAsync(() -> {
            Try<Optional<ApplicationUser>> catching = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveUsersByUsername(user.getName(), true)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute()
                    .stream()
                    .findFirst();
            });

            catching.catching(NotFoundException.class, e -> {
                throw new RuntimeException(new UserDoesntExistException(user.getName()));
            });

            ApplicationUser applicationUser = catching.unwrap()
                .orElseThrow(() -> new RuntimeException(new UserDoesntExistException(user.getName())));

            bridge.getApplication()
                .getUserManager()
                .deleteUser(applicationUser)
                .execute(true);
        }, bridge.getWorker()).thenApply(unused -> user);
    }
}
