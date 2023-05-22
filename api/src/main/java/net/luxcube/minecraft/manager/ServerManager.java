package net.luxcube.minecraft.manager;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * @author Luiz O. F. Corrêa
 * @since 22/05/2023
 **/
public interface ServerManager {

    /**
     * Set the cpu of the server
     * @param cpu The cpu to be set
     * @return A future of the completable void.
     */
    CompletableFuture<Void> setCPU(int cpu);

    /**
     * Set the ram of the server
     * @param ram The ram to be set
     * @return A future of the completable void.
     */
    CompletableFuture<Void> setRam(int ram);

    /**
     * Set the disk of the server
     * @param disk The disk to be set
     * @return A future of the completable void.
     */
    CompletableFuture<Void> setDisk(int disk);

    /**
     * Apply the domain to the server
     * @param domain The domain to be applied
     * @return A future of the completable boolean.
     */
    CompletableFuture<Boolean> setDomain(@NotNull String domain);
}
