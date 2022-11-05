package net.luxcube.minecraft.exception;

/**
 * Thrown when the user does not exist when you are trying to create a server.
 * It's mostly used when the future is completed exceptionally.
 *
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class UserDoesntExistException extends RuntimeException {

    public UserDoesntExistException(String message) {
        super(message);
    }

}
