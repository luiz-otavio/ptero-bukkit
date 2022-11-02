package me.luizotavio.minecraft.exception;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class InsufficientPortRangeException extends RuntimeException {

    public InsufficientPortRangeException() {
        super("The server does not have enough ports to start.");
    }
}
