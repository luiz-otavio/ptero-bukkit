package me.luizotavio.minecraft.server.usage;

import lombok.RequiredArgsConstructor;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
@RequiredArgsConstructor
public class ServerUsageImpl implements ServerUsage {

    private final int memory;
    private final int cpu;
    private final int disk;

    @Override
    public int getMemory() {
        return memory;
    }

    @Override
    public int getDisk() {
        return disk;
    }

    @Override
    public int getCPU() {
        return cpu;
    }
}
