package net.luxcube.minecraft.exception;

/**
 *  Thrown when the making of a server fails because it already exists.
 *  It's mostly used when the future is completed exceptionally.
 *
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class ServerAlreadyExistsException extends RuntimeException {

    public ServerAlreadyExistsException(String message) {
        super(message);
    }

}
