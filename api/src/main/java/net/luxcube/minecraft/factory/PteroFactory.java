package net.luxcube.minecraft.factory;

import net.luxcube.minecraft.exception.*;
import net.luxcube.minecraft.server.PteroServer;
import net.luxcube.minecraft.user.PteroUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Main factory to create servers and users.
 * It automatically inserts the user/server into the pterodactyl, so you don't need to mind
 * about inserting it.
 * <p>
 * Most of methods are async, so you need to catch some specific exceptions such as:
 * * {@link UserDoesntExistException} - Thrown when the user does not exist when you are trying to create a server.
 * * {@link EggDoesntExistException} - Thrown when the egg does not exist when you are trying to create a server.
 * * {@link ServerAlreadyExistsException} - Thrown when the server already exists when you are trying to create a server.
 * * {@link UserAlreadyExistsException} - Thrown when the user already exists when you are trying to create a user.
 * * {@link InsufficientResourcesException} - Thrown when the server cannot be created because there is no resources available.
 * * {@link ServerDoesntExistException} - Thrown when the server does not exist when you are trying to delete a server.
 * <p>
 * You need to catch these exceptions because they are thrown when the future is completed exceptionally.
 * Otherwise, you can just use the {@link CompletableFuture#join()} method to get nullable values.
 *
 * @author Luiz O. F. Corrêa
 * @since 31/10/2022
 **/
public interface PteroFactory {

    /**
     * Creates a new pterodactyl's server into the dashboard.
     * <p>
     * Thrown a lot of exceptions, so you need to catch them, such as:
     * * {@link UserDoesntExistException} - Thrown when the user does not exist when you are trying to create a server.
     * * {@link EggDoesntExistException} - Thrown when the egg does not exist when you are trying to create a server.
     * * {@link ServerAlreadyExistsException} - Thrown when the server already exists when you are trying to create a server.
     * * {@link InsufficientResourcesException} - Thrown when the server cannot be created because there is no resources available.
     *
     * @param name           The name of the server.
     * @param owner          The owner of the server.
     * @param egg            The egg of the server.
     * @param dockerImage    The docker image of the server.
     * @param startupCommand The startup command of the server.
     * @param memory         The memory of the server.
     * @param disk           The disk of the server.
     * @param cpu            The limit of using CPU of the server. (0-100 for each core, so you can do 100 * 2 for 2 cores)
     * @return A future of the completable server.
     */
    CompletableFuture<PteroServer> createServer(
        @NotNull String name,
        @NotNull PteroUser owner,
        @NotNull String egg,
        @NotNull String dockerImage,
        @NotNull String startupCommand,
        int memory,
        int disk,
        int cpu
    );

    /**
     * Creates a new pterodactyl's user into the dashboard.
     * <p>
     * Thrown some exceptions, so you need to catch them, such as:
     * * {@link UserAlreadyExistsException} - Thrown when the user already exists when you are trying to create a user.
     *
     * @param uuid     The uuid of the user.
     * @param username The username of the user.
     * @param password The password of the user.
     * @param email    The email of the user, if it's null, it will be username@luxcube.net.
     * @return A future of the completable user.
     */
    CompletableFuture<PteroUser> createUser(
        @NotNull UUID uuid,
        @NotNull String username,
        @NotNull String password,
        @Nullable String email
    );


}
