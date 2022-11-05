package me.luizotavio.minecraft.exception;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class ServerAlreadyExistsException extends RuntimeException {

    public ServerAlreadyExistsException(String message) {
        super(message);
    }

}
