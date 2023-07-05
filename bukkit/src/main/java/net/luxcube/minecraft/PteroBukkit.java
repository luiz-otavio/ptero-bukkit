package net.luxcube.minecraft;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.NotNull;

/**
 * @author Luiz O. F. Corrêa
 * @since 05/11/2022
 **/
public class PteroBukkit {

    /**
     * Registers the PteroManager service.
     *
     * @param url            The URL of the Pterodactyl panel.
     * @param applicationKey The application key of the Pterodactyl panel.
     * @param clientKey      The client key of the Pterodactyl panel.
     * @param nThreads       The number of threads to be used.
     * @param plugin         The plugin that will be used to register the service.
     * @return The PteroManager service.
     * @throws IllegalArgumentException If nThreads is less than or equal to 0 or if the plugin is null.
     */
    public static PteroManager createInstance(
        @NotNull String url,
        @NotNull String applicationKey,
        @NotNull String clientKey,
        int nThreads,
        @NotNull Plugin plugin
    ) throws IllegalArgumentException {
        if (!plugin.isEnabled()) {
            throw new IllegalArgumentException("Plugin must be enabled");
        }

        if (nThreads <= 0) {
            throw new IllegalArgumentException("nThreads must be greater than 0");
        }

        // Don't need to load it again
        if (Bukkit.getServicesManager().isProvidedFor(PteroManager.class)) {
            return Bukkit.getServicesManager().load(PteroManager.class);
        }

        PteroManager manager = new PteroManagerImpl(
            applicationKey,
            clientKey,
            url,
            nThreads
        );

        Bukkit.getServicesManager().register(PteroManager.class, manager, plugin, ServicePriority.Normal);

        return manager;
    }

}
