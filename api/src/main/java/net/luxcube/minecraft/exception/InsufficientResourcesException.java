package net.luxcube.minecraft.exception;

/**
 * Thrown when the making of a server fails because there are not enough resources.
 * It means such as not enough memory, disk or cpu or even there are no nodes available.
 * It's mostly used when the future is completed exceptionally.
 *
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class InsufficientResourcesException extends RuntimeException {

    public InsufficientResourcesException() {
        super("The server does not have enough resources to start.");
    }
}
