package me.luizotavio.minecraft.exception;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class InsufficientAllocationsException extends RuntimeException {

    public InsufficientAllocationsException() {
        super("The server does not have enough allocations to start.");
    }
}
