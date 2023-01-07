package net.luxcube.minecraft.vo;

import com.mattmalec.pterodactyl4j.PteroBuilder;
import com.mattmalec.pterodactyl4j.application.entities.PteroApplication;
import com.mattmalec.pterodactyl4j.client.entities.PteroClient;
import com.mattmalec.pterodactyl4j.exceptions.LoginException;
import com.mattmalec.pterodactyl4j.utils.NamedThreadFactory;
import lombok.Getter;
import net.luxcube.minecraft.logger.PteroLogger;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
@Getter
public class PteroBridgeVO {

    public static PteroBridgeVO createInstance(
        @NotNull URL address,
        @NotNull String clientKey,
        @NotNull String applicationKey,
        int nThreads
    ) {
        if (!address.toString().endsWith("/")) {
            try {
                address = new URL(address + "/");
            } catch (MalformedURLException e) {
                PteroLogger.severe("Invalid URL: " + address, e);
                return null;
            }
        }

        if (nThreads < 1) {
            PteroLogger.severe("Invalid number of threads: " + nThreads);
            nThreads = 1;
        }

        ExecutorService executorService = new ThreadPoolExecutor(
            nThreads,
            nThreads,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            new NamedThreadFactory("PteroMC")
        );

        PteroLogger.debug("PteroBridge initialized with %d threads", nThreads);
        return new PteroBridgeVO(
            address,
            clientKey,
            applicationKey,
            PteroBuilder.createApplication(address.toString(), applicationKey),
            PteroBuilder.createClient(address.toString(), clientKey),
            executorService
        );
    }

    private final PteroClient client;
    private final PteroApplication application;

    private final URL address;

    private final String clientKey;
    private final String applicationKey;

    private final ExecutorService worker;

    private PteroBridgeVO(
        @NotNull URL address,
        @NotNull String clientKey,
        @NotNull String applicationKey,
        @NotNull PteroApplication application,
        @NotNull PteroClient client,
        @NotNull ExecutorService worker
    ) {
        this.address = address;
        this.clientKey = clientKey;
        this.applicationKey = applicationKey;
        this.application = application;
        this.client = client;
        this.worker = worker;
    }
}
