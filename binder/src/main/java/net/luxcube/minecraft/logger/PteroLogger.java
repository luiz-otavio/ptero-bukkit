package net.luxcube.minecraft.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class PteroLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("PteroBridge");

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warning(String message) {
        LOGGER.warn(message);
    }

    public static void severe(String message) {
        LOGGER.error(message);
    }

    public static void severe(String message, Object... args) {
        LOGGER.error(message, args);
    }

    public static void debug(String message) {
        LOGGER.debug(message);
    }

    public static void debug(String message, Object... args) {
        LOGGER.debug(String.format(message, args));
    }

    public static void severe(String message, Throwable throwable) {
        LOGGER.error(message);
        throwable.printStackTrace();
    }

}
