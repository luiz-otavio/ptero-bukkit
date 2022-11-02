package me.luizotavio.minecraft.user;

import me.luizotavio.minecraft.server.PteroServer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    void setName(@NotNull String name);

    void setEmail(@NotNull String email);

    void setPassword(@NotNull String password);

}
