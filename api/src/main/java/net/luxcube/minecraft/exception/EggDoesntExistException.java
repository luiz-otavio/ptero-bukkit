package net.luxcube.minecraft.exception;

/**
 * Thrown when the egg does not exist when you are trying to create a server.
 * It's mostly used when the future is completed exceptionally.
 *
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class EggDoesntExistException extends RuntimeException {

    public EggDoesntExistException(String message) {
        super(message);
    }

}
