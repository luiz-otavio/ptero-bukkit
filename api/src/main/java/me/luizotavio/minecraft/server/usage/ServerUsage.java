package me.luizotavio.minecraft.server.usage;

/**
 * @author Luiz O. F. Corrêa
 * @since 31/10/2022
 **/
public interface ServerUsage {

    int getMemory();

    int getDisk();

    int getCPU();

}
