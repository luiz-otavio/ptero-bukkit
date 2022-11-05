package net.luxcube.minecraft;

import net.luxcube.minecraft.factory.PteroFactory;
import net.luxcube.minecraft.factory.PteroFactoryImpl;
import net.luxcube.minecraft.repository.server.ServerRepository;
import net.luxcube.minecraft.repository.server.ServerRepositoryImpl;
import net.luxcube.minecraft.repository.user.UserRepository;
import net.luxcube.minecraft.repository.user.UserRepositoryImpl;
import net.luxcube.minecraft.vo.PteroBridgeVO;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class PteroManagerImpl implements PteroManager {

    private final PteroFactory factory;

    private final ServerRepository serverRepository;
    private final UserRepository userRepository;

    private final String applicationKey, clientKey;

    private final URL url;


    public PteroManagerImpl(
        @NotNull String applicationKey,
        @NotNull String clientKey,
        @NotNull String url,
        int nThreads
    ) {
        if (nThreads <= 0) {
            throw new IllegalArgumentException("nThreads must be greater than 0");
        }

        this.applicationKey = applicationKey;
        this.clientKey = clientKey;

        URL targetUrl = null;
        try {
            targetUrl = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } finally {
            this.url = targetUrl;
        }

        PteroBridgeVO pteroBridgeVO = PteroBridgeVO.createInstance(
            targetUrl,
            clientKey,
            applicationKey,
            nThreads
        );

        if (pteroBridgeVO == null) {
            throw new RuntimeException("Invalid Pterodactyl bridge configuration");
        }

        this.factory = new PteroFactoryImpl(pteroBridgeVO);
        this.serverRepository = new ServerRepositoryImpl(pteroBridgeVO);
        this.userRepository = new UserRepositoryImpl(pteroBridgeVO);
    }

    public PteroManagerImpl(
        @NotNull PteroFactory factory,
        @NotNull ServerRepository serverRepository,
        @NotNull UserRepository userRepository,
        @NotNull String applicationKey,
        @NotNull String clientKey,
        @NotNull URL url,
        @NotNull PteroBridgeVO bridgeVO
    ) {
        this.factory = factory;
        this.serverRepository = serverRepository;
        this.userRepository = userRepository;
        this.applicationKey = applicationKey;
        this.clientKey = clientKey;
        this.url = url;
    }


    @Override
    public @NotNull UserRepository getUserRepository() {
        return userRepository;
    }

    @Override
    public @NotNull PteroFactory getFactory() {
        return factory;
    }

    @Override
    public @NotNull ServerRepository getServerRepository() {
        return serverRepository;
    }

    @Override
    public @NotNull String getApplicationKey() {
        return applicationKey;
    }

    @Override
    public @NotNull String getClientKey() {
        return clientKey;
    }

    @Override
    public @NotNull URL getURL() {
        return url;
    }

}
