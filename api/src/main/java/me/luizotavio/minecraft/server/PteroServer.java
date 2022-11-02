package me.luizotavio.minecraft.server;

import me.luizotavio.minecraft.server.status.StatusType;
import me.luizotavio.minecraft.server.usage.ServerUsage;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Luiz O. F. Corrêa
 * @since 31/10/2022
 **/
public interface PteroServer {

    @NotNull
    String getIdentifier();

    @NotNull
    UUID getUUID();

    @NotNull
    String getAddress();

    @NotNull
    String getNode();

    @NotNull
    CompletableFuture<StatusType> getStatus();

    CompletableFuture<ServerUsage> getUsage();

    CompletableFuture<Void> start();

    CompletableFuture<Void> stop();

}
