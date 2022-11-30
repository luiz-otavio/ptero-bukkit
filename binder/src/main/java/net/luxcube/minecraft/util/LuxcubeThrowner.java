package net.luxcube.minecraft.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Luiz O. F. Corrêa
 * @since 30/11/2022
 **/
public class LuxcubeThrowner<K extends Exception, E extends Exception> implements Consumer<K> {

    private final Supplier<E> supplier;

    public LuxcubeThrowner(Supplier<E> supplier) {
        this.supplier = supplier;
    }

    @Override
    public void accept(K e) {
        try {
            throw supplier.get();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
