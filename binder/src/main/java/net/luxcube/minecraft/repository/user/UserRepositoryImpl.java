package net.luxcube.minecraft.repository.user;

import com.mattmalec.pterodactyl4j.application.entities.ApplicationUser;
import com.mattmalec.pterodactyl4j.exceptions.NotFoundException;
import net.luxcube.minecraft.exception.UserDoesntExistException;
import net.luxcube.minecraft.logger.PteroLogger;
import net.luxcube.minecraft.user.PteroUser;
import net.luxcube.minecraft.user.PteroUserImpl;
import net.luxcube.minecraft.util.Try;
import net.luxcube.minecraft.util.Users;
import net.luxcube.minecraft.vo.PteroBridgeVO;
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
        PteroLogger.debug("Searching for user by username: %s", username);

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
                throw new UserDoesntExistException(username);
            });

            return catching.unwrap()
                .orElseThrow(() -> new UserDoesntExistException(username));
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
        PteroLogger.debug("Searching for user by UUID: %s", uuid.toString());

        String fromShort = Users.fromShort(uuid);

        return CompletableFuture.supplyAsync(() -> {
            return bridge.getApplication()
                .retrieveUsers()
                .cache(true)
                .timeout(10, TimeUnit.SECONDS)
                .stream()
                .filter(target -> target.getFirstName().startsWith(fromShort)).findAny()
                .orElseThrow(() -> new UserDoesntExistException(uuid.toString()));
        }, bridge.getWorker()).whenComplete((user, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            }
        }).thenApply(user -> {
            return new PteroUserImpl(
                bridge,
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                uuid,
                null
            );
        });
    }

    @Override
    public CompletableFuture<PteroUser> findUserByEmail(@NotNull String email) {
        PteroLogger.debug("Searching for user by email: %s", email);

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
                throw new UserDoesntExistException(email);
            });

            return applicationUser.unwrap()
                .orElseThrow(() -> new UserDoesntExistException(email));
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
        PteroLogger.debug("Searching for user by snowflake: %s", snowflake);

        return CompletableFuture.supplyAsync(() -> {
            Try<ApplicationUser> catching = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveUserById(snowflake)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute();
            });

            catching.catching(NotFoundException.class, e -> {
                throw new UserDoesntExistException(snowflake);
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
        PteroLogger.debug("Deleting user: %s", user.getName());

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
                throw new UserDoesntExistException(user.getName());
            });

            ApplicationUser applicationUser = catching.unwrap()
                .orElseThrow(() -> new UserDoesntExistException(user.getName()));

            bridge.getApplication()
                .getUserManager()
                .deleteUser(applicationUser)
                .execute(true);
        }, bridge.getWorker()).thenApply(unused -> user);
    }
}
