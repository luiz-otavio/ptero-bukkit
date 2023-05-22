package net.luxcube.minecraft;

import org.jetbrains.annotations.NotNull;

/**
 * @author Luiz O. F. Corrêa
 * @since 22/05/2023
 **/
public class PteroBungee {

    /**
     * Registers the PteroManager service.
     * @param url The URL of the Pterodactyl panel.
     * @param applicationKey The application key of the Pterodactyl panel.
     * @param clientKey The client key of the Pterodactyl panel.
     * @param nThreads The number of threads to be used.
     * @return The PteroManager service.
     * @throws IllegalArgumentException If nThreads is less than or equal to 0 or if the plugin is null.
     */
    public static PteroManager createInstance(
        @NotNull String url,
        @NotNull String applicationKey,
        @NotNull String clientKey,
        int nThreads
    ) throws IllegalArgumentException {
        if (nThreads <= 0) {
            throw new IllegalArgumentException("nThreads must be greater than 0");
        }

        return new PteroManagerImpl(
            applicationKey,
            clientKey,
            url,
            nThreads
        );
    }

}
