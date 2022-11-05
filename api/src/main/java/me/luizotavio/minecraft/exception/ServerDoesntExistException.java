package me.luizotavio.minecraft.exception;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class ServerDoesntExistException extends RuntimeException {

    public ServerDoesntExistException(String message) {
        super(message);
    }

}
