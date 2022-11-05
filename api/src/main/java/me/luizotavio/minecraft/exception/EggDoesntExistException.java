package me.luizotavio.minecraft.exception;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class EggDoesntExistException extends RuntimeException {

    public EggDoesntExistException(String message) {
        super(message);
    }

}
