package me.luizotavio.minecraft.user;

import me.luizotavio.minecraft.server.PteroServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a pterodactyl's user with less information than the Pterodactyl's API.
 * It's unique mainly because it's a wrapper of the Pterodactyl's API.
 * Also, it gives some useful methods to get the user's servers.
 *
 * @author Luiz O. F. Corrêa
 * @since 31/10/2022
 **/
public interface PteroUser {

    /**
     * Retrieve the identifier of the user.
     * @return The identifier of the user.
     */
    @NotNull
    String getId();

    /**
     * Retrieve the UUID of the user.
     * @return The UUID of the user or nil.
     */
    @Nullable UUID getUniqueId();

    /**
     * Retrieve the username of the user.
     * @return The username of the user.
     */
    @NotNull
    String getName();

    /**
     * Retrieve the email of the user.
     * @return The email of the user.
     */
    @NotNull
    String getEmail();

    /**
     * Retrieve the password of the user.
     * @return The password of the user or nil.
     */
    @Nullable String getPassword();

    /**
     * Retrieve the collection of servers of the user.
     * @return A future of the completable collection of servers.
     */
    @NotNull
    CompletableFuture<List<PteroServer>> getServers();

    /**
     * Update the name of the user.
     * @param name The new name of the user.
     * @return A future of the completable user.
     */
    CompletableFuture<Void> setName(@NotNull String name);

    /**
     * Update the email of the user.
     * @param email The new email of the user.
     * @return A future of the completable user.
     */
    CompletableFuture<Void> setEmail(@NotNull String email);

    /**
     * Update the password of the user.
     * @param password The new password of the user.
     * @return A future of the completable user.
     */
    CompletableFuture<Void> setPassword(@NotNull String password);

}
