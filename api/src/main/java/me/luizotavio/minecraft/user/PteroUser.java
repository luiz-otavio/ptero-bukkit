package me.luizotavio.minecraft.user;

import me.luizotavio.minecraft.server.PteroServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Luiz O. F. Corrêa
 * @since 31/10/2022
 **/
public interface PteroUser {

    @NotNull
    String getId();

    @Nullable UUID getUniqueId();

    @NotNull
    String getName();

    @NotNull
    String getEmail();

    @Nullable String getPassword();

    @NotNull
    CompletableFuture<List<PteroServer>> getServers();

    CompletableFuture<Void> setName(@NotNull String name);

    CompletableFuture<Void> setEmail(@NotNull String email);

    CompletableFuture<Void> setPassword(@NotNull String password);

}
