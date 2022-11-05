package me.luizotavio.minecraft;

import me.luizotavio.minecraft.factory.PteroFactory;
import me.luizotavio.minecraft.repository.server.ServerRepository;
import me.luizotavio.minecraft.repository.user.UserRepository;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

/**
 * Represents the main focus of the Pterodactyl's API.
 * It's unique mainly because it's a wrapper of the Pterodactyl's API.
 * Also, it gives some useful methods to get things such as:
 *  * The user's repository.
 *  * The server's repository.
 *  * The factory of the API.
 *  * The URL of the API.
 *  * The API's key.
 *
 * @author Luiz O. F. Corrêa
 * @since 31/10/2022
 **/
public interface PteroManager {

    /**
     * Retrieve the user's repository.
     * @return The user's repository.
     */
    @NotNull UserRepository getUserRepository();

    /**
     * Retrieve the factory of the API.
     * @return The factory of the API.
     */
    @NotNull PteroFactory getFactory();

    /**
     * Retrieve the server's repository.
     * @return The server's repository.
     */
    @NotNull ServerRepository getServerRepository();

    /**
     * Retrieve the application key.
     * @return The application key.
     */
    @NotNull String getApplicationKey();

    /**
     * Retrieve the client key.
     * @return The client key.
     */
    @NotNull String getClientKey();

    /**
     * Retrieve the URL of the API.
     * @return The URL of the API.
     */
    @NotNull URL getURL();

}
