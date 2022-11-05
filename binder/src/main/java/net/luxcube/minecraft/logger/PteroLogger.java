package net.luxcube.minecraft.logger;

import java.util.logging.Logger;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class PteroLogger {

    private static final Logger LOGGER = Logger.getLogger("PteroMC");

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warning(String message) {
        LOGGER.warning(message);
    }

    public static void severe(String message) {
        LOGGER.severe(message);
    }

    public static void debug(String message) {
        LOGGER.fine(message);
    }

    public static void debug(String message, Object... args) {
        LOGGER.fine(String.format(message, args));
    }

    public static void severe(String message, Throwable throwable) {
        LOGGER.severe(message);
        throwable.printStackTrace();
    }

}
