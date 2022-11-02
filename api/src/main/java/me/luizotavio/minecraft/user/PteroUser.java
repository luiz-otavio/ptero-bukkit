package me.luizotavio.minecraft.user;

import me.luizotavio.minecraft.server.PteroServer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Luiz O. F. Corrêa
 * @since 31/10/2022
 **/
public interface PteroUser {

    @NotNull
    String getId();

    @NotNull
    String getName();

    @NotNull
    String getEmail();

    @NotNull
    List<PteroServer> getServers();

    CompletableFuture<Void> setName(@NotNull String name);

    CompletableFuture<Void> setEmail(@NotNull String email);

    CompletableFuture<Void> setPassword(@NotNull String password);

}
