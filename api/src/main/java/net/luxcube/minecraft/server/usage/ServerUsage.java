package net.luxcube.minecraft.server.usage;

/**
 * Represents the usage of the server.
 *
 * @author Luiz O. F. Corrêa
 * @since 31/10/2022
 **/
public interface ServerUsage {

    int getMemory();

    int getDisk();

    int getCPU();

}
