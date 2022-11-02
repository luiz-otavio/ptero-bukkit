package me.luizotavio.minecraft.repository.user;

import me.luizotavio.minecraft.user.PteroUser;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public interface UserRepository {

    CompletableFuture<PteroUser> findUserByUsername(@NotNull String username);

    CompletableFuture<PteroUser> findUserByUUID(@NotNull UUID uuid);

    CompletableFuture<PteroUser> findUserByEmail(@NotNull String email);

    CompletableFuture<PteroUser> findUserBySnowflake(@NotNull String snowflake);

    CompletableFuture<PteroUser> insertUser(@NotNull PteroUser user);

    CompletableFuture<PteroUser> deleteUser(@NotNull PteroUser user);

}
