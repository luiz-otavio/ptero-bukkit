package net.luxcube.minecraft.util;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class Users {

    public static String fromShort(@NotNull UUID uuid) {
        return uuid.toString()
            .substring(0, 8);
    }

}
