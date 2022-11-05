package net.luxcube.minecraft.exception;

/**
 * Thrown when the filtering or searching of a server returns no results.
 * It's mostly used when the future is completed exceptionally.
 *
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class ServerDoesntExistException extends RuntimeException {

    public ServerDoesntExistException(String message) {
        super(message);
    }

}
