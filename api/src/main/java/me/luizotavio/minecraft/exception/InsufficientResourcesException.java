package me.luizotavio.minecraft.exception;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class InsufficientResourcesException extends RuntimeException {

    public InsufficientResourcesException() {
        super("The server does not have enough resources to start.");
    }
}
