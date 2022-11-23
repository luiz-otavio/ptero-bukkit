package net.luxcube.minecraft.repository.server;

import net.luxcube.minecraft.exception.ServerDoesntExistException;
import net.luxcube.minecraft.server.PteroServer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Main repository to lookup for servers based on UUID, name or identifier.
 * The identifier can be understood as a snowflake, so it's a unique identifier
 *
 * All methods of this interface are async, so you need to catch some specific exceptions such as:
 *  * {@link ServerDoesntExistException} - Thrown when the server does not exist when you are trying to lookup for a server.
 *
 * You need to catch these exceptions because they are thrown when the future is completed exceptionally.
 * Otherwise, you can just use the {@link CompletableFuture#join()} method to get nullable values.
 *
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public interface ServerRepository {

    /**
     * Looks up for a server based on the UUID.
     * @param uuid The UUID of the server.
     * @return A future of the completable server.
     */
    CompletableFuture<PteroServer> findServerByUUID(@NotNull UUID uuid);

    /**
     * Looks up for a server based on the name.
     * @param name The name of the server.
     * @return A future of the completable server.
     */
    CompletableFuture<PteroServer> findServerByName(@NotNull String name);

    /**
     * Looks up for a server based on the identifier.
     * @param snowflake The identifier of the server.
     * @return A future of the completable server.
     */
    CompletableFuture<PteroServer> findServerBySnowflake(@NotNull String snowflake);

    /**
     * Deletes the server from the pterodactyl.
     * @param server The server to be deleted.
     * @return A future of the completable server.
     */
    CompletableFuture<PteroServer> deleteServer(@NotNull PteroServer server);

    /**
     * Lists all servers from the pterodactyl.
     * @return A future of the completable list of servers.
     */
    CompletableFuture<List<PteroServer>> retrieveServersByPage(int page, int size);

}
