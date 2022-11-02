package me.luizotavio.minecraft.exception;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException() {
        super("The user already exists.");
    }
}
