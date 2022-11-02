package me.luizotavio.minecraft;

import me.luizotavio.minecraft.factory.PteroFactory;
import me.luizotavio.minecraft.repository.server.ServerRepository;
import me.luizotavio.minecraft.repository.user.UserRepository;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

/**
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
