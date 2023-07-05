package net.luxcube.minecraft.repository.user;

import net.luxcube.minecraft.exception.UserDoesntExistException;
import net.luxcube.minecraft.user.PteroUser;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Main repository to lookup for users based on UUID, name, email or identifier.
 * The identifier can be understood as a snowflake, so it's a unique identifier
 * <p>
 * All methods of this interface are async, so you need to catch some specific exceptions such as:
 * * {@link UserDoesntExistException} - Thrown when the user does not exist when you are trying to lookup for a user.
 * <p>
 * You need to catch these exceptions because they are thrown when the future is completed exceptionally.
 * Otherwise, you can just use the {@link CompletableFuture#join()} method to get nullable values.
 *
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public interface UserRepository {

    /**
     * Looks up for a user based on the username.
     * Also, it's case sensitive.
     *
     * @param username The username of the user.
     * @return A future of the completable user.
     */
    CompletableFuture<PteroUser> findUserByUsername(@NotNull String username);

    /**
     * Look up for a user based on the UUID.
     * Also, it's not the same as Player's UUID, it's the UUID of the pterodactyl's user.
     *
     * @param uuid The UUID of the user.
     * @return A future of the completable user.
     */
    CompletableFuture<PteroUser> findUserByUUID(@NotNull UUID uuid);

    /**
     * Looks up for a user based on the email.
     *
     * @param email The email of the user.
     * @return A future of the completable user.
     */
    CompletableFuture<PteroUser> findUserByEmail(@NotNull String email);

    /**
     * Looks up for a user based on the identifier.
     *
     * @param snowflake The identifier of the user.
     * @return A future of the completable user.
     */
    CompletableFuture<PteroUser> findUserBySnowflake(@NotNull String snowflake);

    /**
     * Deletes the user from the pterodactyl.
     *
     * @param user The user to be deleted.
     * @return A future of the completable user.
     */
    CompletableFuture<PteroUser> deleteUser(@NotNull PteroUser user);

}
