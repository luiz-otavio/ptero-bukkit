package me.luizotavio.minecraft.repository.server;

import me.luizotavio.minecraft.server.PteroServer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public interface ServerRepository {

    CompletableFuture<PteroServer> findServerByUUID(@NotNull UUID uuid);

    CompletableFuture<PteroServer> findServerByName(@NotNull String name);

    CompletableFuture<PteroServer> findServerBySnowflake(@NotNull String snowflake);

    CompletableFuture<PteroServer> deleteServer(@NotNull PteroServer server);

    CompletableFuture<PteroServer> insertServer(@NotNull PteroServer server);

}
