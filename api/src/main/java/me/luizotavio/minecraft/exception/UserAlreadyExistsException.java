package me.luizotavio.minecraft.exception;

/**
 * Thrown when the user already exists when you are trying to create a user.
 * It's mostly used when the future is completed exceptionally.
 *
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException() {
        super("The user already exists.");
    }
}
