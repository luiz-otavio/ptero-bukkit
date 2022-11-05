package net.luxcube.minecraft.server;

import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.client.entities.Utilization;
import com.mattmalec.pterodactyl4j.exceptions.NotFoundException;
import net.luxcube.minecraft.exception.ServerDoesntExistException;
import net.luxcube.minecraft.logger.PteroLogger;
import net.luxcube.minecraft.server.status.StatusType;
import net.luxcube.minecraft.server.usage.ServerUsage;
import net.luxcube.minecraft.server.usage.ServerUsageImpl;
import net.luxcube.minecraft.util.Try;
import net.luxcube.minecraft.vo.PteroBridgeVO;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class PteroServerImpl implements PteroServer {

    protected PteroBridgeVO bridge;

    private final String identifier;
    private final String address;
    private final String node;

    private final UUID uuid;

    public PteroServerImpl(
        @NotNull PteroBridgeVO bridge,
        @NotNull String identifier,
        @NotNull String address,
        @NotNull String node,
        @NotNull UUID uuid
    ) {
        this.bridge = bridge;
        this.identifier = identifier;
        this.address = address;
        this.node = node;
        this.uuid = uuid;
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public @NotNull UUID getUUID() {
        return uuid;
    }

    @Override
    public @NotNull String getAddress() {
        return address;
    }

    @Override
    public @NotNull String getNode() {
        return node;
    }

    @Override
    public @NotNull CompletableFuture<StatusType> getStatus() {
        PteroLogger.debug("Getting server status from server %s", identifier);

        return CompletableFuture.supplyAsync(() -> {
            Try<ClientServer> catching = Try.catching(() -> {
                return bridge.getClient()
                    .retrieveServerByIdentifier(identifier)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute();
            });

            catching.catching(NotFoundException.class, e -> {
                throw new ServerDoesntExistException(identifier);
            });

            return catching.unwrap();
        }, bridge.getWorker()).thenApply(clientServer -> {
            Utilization utilization = clientServer.retrieveUtilization()
                .execute();

            StatusType statusType;
            switch (utilization.getState()) {
                case STARTING -> statusType = StatusType.STARTING;
                case RUNNING -> statusType = StatusType.ONLINE;
                case STOPPING -> statusType = StatusType.STOPPING;
                default -> statusType = StatusType.OFFLINE;
            }

            return statusType;
        });
    }

    @Override
    public CompletableFuture<ServerUsage> getUsage() {
        PteroLogger.debug("Getting usage of server %s", identifier);

        return CompletableFuture.supplyAsync(() -> {
            Try<ClientServer> catching = Try.catching(() -> {
                return bridge.getClient()
                    .retrieveServerByIdentifier(identifier)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute();
            });

            catching.catching(NotFoundException.class, e -> {
                throw new ServerDoesntExistException(identifier);
            });

            return catching.unwrap();
        }, bridge.getWorker()).thenApply(clientServer -> {
            Utilization utilization = clientServer.retrieveUtilization()
                .execute();

            return new ServerUsageImpl(
                Math.max(0, (int) utilization.getMemory()),
                Math.max(0, (int) utilization.getCPU()),
                Math.max(0, (int) utilization.getDisk())
            );
        });
    }

    @Override
    public CompletableFuture<Void> start() {
        PteroLogger.debug("Starting server %s", identifier);

        return CompletableFuture.runAsync(() -> {
            Try<ClientServer> catching = Try.catching(() -> {
                return bridge.getClient()
                    .retrieveServerByIdentifier(identifier)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute();
            });

            catching.catching(NotFoundException.class, e -> {
                throw new ServerDoesntExistException(identifier);
            });

            catching.unwrap()
                .start()
                .execute();
        }, bridge.getWorker());
    }

    @Override
    public CompletableFuture<Void> stop() {
        PteroLogger.debug("Stopping server %s", identifier);

        return CompletableFuture.runAsync(() -> {
            Try<ClientServer> catching = Try.catching(() -> {
                return bridge.getClient()
                    .retrieveServerByIdentifier(identifier)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute();
            });

            catching.catching(NotFoundException.class, e -> {
                throw new ServerDoesntExistException(identifier);
            });

            catching.unwrap()
                .stop()
                .execute();
        }, bridge.getWorker());
    }
}
