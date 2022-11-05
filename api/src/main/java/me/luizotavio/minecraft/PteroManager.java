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

    @NotNull UserRepository getUserRepository();

    @NotNull PteroFactory getFactory();

    @NotNull ServerRepository getServerRepository();

    @NotNull String getApplicationKey();

    @NotNull String getClientKey();

    @NotNull URL getURL();

}
