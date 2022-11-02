package me.luizotavio.minecraft.factory;

import me.luizotavio.minecraft.exception.InsufficientAllocationsException;
import me.luizotavio.minecraft.exception.InsufficientPortRangeException;
import me.luizotavio.minecraft.exception.InsufficientResourcesException;
import me.luizotavio.minecraft.exception.UserAlreadyExistsException;
import me.luizotavio.minecraft.server.PteroServer;
import me.luizotavio.minecraft.user.PteroUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Luiz O. F. Corrêa
 * @since 31/10/2022
 **/
public interface PteroFactory {

    CompletableFuture<PteroServer> createServer(
        @NotNull String name,
        @NotNull PteroUser owner,
        @NotNull String egg,
        @NotNull String dockerImage,
        @NotNull String startupCommand,
        int memory,
        int disk,
        int cpu
    ) throws InsufficientAllocationsException, InsufficientPortRangeException, InsufficientResourcesException;

    CompletableFuture<PteroUser> createUser(
        @NotNull UUID uuid,
        @NotNull String username,
        @NotNull String password,
        @Nullable String email
    ) throws UserAlreadyExistsException;



}
