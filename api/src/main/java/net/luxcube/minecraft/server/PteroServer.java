package net.luxcube.minecraft.server;

import net.luxcube.minecraft.manager.ServerManager;
import net.luxcube.minecraft.server.status.StatusType;
import net.luxcube.minecraft.server.usage.ServerUsage;
import net.luxcube.minecraft.user.PteroUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a pterodactyl's server with less information than the Pterodactyl's API.
 * It's unique mainly because it's a wrapper of the Pterodactyl's API.
 * Also, it gives some useful methods to get the server's status and usage.
 *
 * @author Luiz O. F. Corrêa
 * @since 31/10/2022
 **/
public interface PteroServer {

    /**
     * Retrieve the identifier of the server.
     * @return The identifier of the server.
     */
    @NotNull
    String getIdentifier();

    /**
     * Retrieve the name of the server.
     * @return The name of the server.
     */
    @NotNull
    String getName();

    /**
     * Retrieve the UUID of the server.
     * @return The UUID of the server.
     */
    @NotNull
    UUID getUUID();

    /**
     * Retrieve the full address of the server.
     * @return The full address of the server.
     */
    @NotNull
    String getAddress();

    /**
     * Retrieve the name of the node.
     * @return The name of the node.
     */
    @NotNull
    String getNode();

    /**
     * Retrieve the manager of the server
     * @return The manager of the server.
     */
    ServerManager getManager();

    /**
     * Retrieve the status of the server.
     * @return A future of the completable status.
     */
    @NotNull
    CompletableFuture<StatusType> getStatus();

    /**
     * Retrieve the usage of the server.
     * @return A future of the completable usage.
     */
    CompletableFuture<ServerUsage> getUsage();

    /**
     * Allow the user to change the state of the server.
     * @param pteroUser The user that will change the state of the server.
     * @return A future of the completable state.
     */
    CompletableFuture<Void> allow(@NotNull PteroUser pteroUser);

    /**
     * Deny the user to change the state of the server.
     * @param pteroUser The user that will change the state of the server.
     * @return A future of the completable state.
     */
    CompletableFuture<Void> disallow(@NotNull PteroUser pteroUser);

    /**
     * Execute the start command of the server.
     * @return A future of the completable server.
     */
    CompletableFuture<Void> start();

    /**
     * Check if user can handle/watch the server.
     * @param pteroUser The user that will check the state of the server.
     * @return A future of the completable state.
     */
    CompletableFuture<Boolean> hasPermission(@NotNull PteroUser pteroUser);

    /**
     * Execute the stop command of the server.
     * @return A future of the completable server.
     */
    CompletableFuture<Void> stop();

}
