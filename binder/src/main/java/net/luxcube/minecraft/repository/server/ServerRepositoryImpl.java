package net.luxcube.minecraft.repository.server;

import com.mattmalec.pterodactyl4j.UtilizationState;
import com.mattmalec.pterodactyl4j.application.entities.ApplicationServer;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.client.entities.Utilization;
import com.mattmalec.pterodactyl4j.exceptions.NotFoundException;
import net.luxcube.minecraft.exception.ServerDoesntExistException;
import net.luxcube.minecraft.logger.PteroLogger;
import net.luxcube.minecraft.server.PteroServer;
import net.luxcube.minecraft.server.PteroServerImpl;
import net.luxcube.minecraft.util.Pair;
import net.luxcube.minecraft.util.Servers;
import net.luxcube.minecraft.util.Try;
import net.luxcube.minecraft.vo.PteroBridgeVO;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class ServerRepositoryImpl implements ServerRepository {

    private final PteroBridgeVO bridge;

    public ServerRepositoryImpl(@NotNull PteroBridgeVO bridge) {
        this.bridge = bridge;
    }

    @Override
    public CompletableFuture<PteroServer> findServerByUUID(@NotNull UUID uuid) {
        PteroLogger.debug("Searching server by UUID: %s", uuid.toString());

        return bridge.getApplication()
            .retrieveServers()
            .cache(true)
            .timeout(10, TimeUnit.SECONDS)
            .takeWhileAsync(1, server -> server.getUUID().equals(uuid))
            .thenApply(collection -> {
                if (collection.isEmpty()) {
                    throw new ServerDoesntExistException(uuid.toString());
                }

                ApplicationServer any = collection.stream()
                    .findAny()
                    .orElseThrow();

                Pair<String, String> addressAndNode = Servers.getAddressAndNode(any);

                return new PteroServerImpl(
                    bridge,
                    any.getIdentifier(),
                    addressAndNode.first(),
                    addressAndNode.second(),
                    any.getName(),
                    any.getUUID()
                );
            });

    }

    @Override
    public CompletableFuture<PteroServer> findServerByName(@NotNull String name) {
        PteroLogger.debug("Searching server by name: %s", name);

        return CompletableFuture.supplyAsync(() -> {
            return bridge.getApplication()
                .retrieveServersByName(name, true)
                .execute();
        }, bridge.getWorker()).thenApply(collection -> {
            if (collection.isEmpty()) {
                throw new ServerDoesntExistException(name);
            }

            ApplicationServer any = collection.stream()
                .findAny()
                .orElseThrow();

            Pair<String, String> addressAndNode = Servers.getAddressAndNode(any);

            return new PteroServerImpl(
                bridge,
                any.getIdentifier(),
                addressAndNode.first(),
                addressAndNode.second(),
                any.getName(),
                any.getUUID()
            );
        });
    }

    @Override
    public CompletableFuture<PteroServer> findServerBySnowflake(@NotNull String snowflake) {
        return CompletableFuture.supplyAsync(() -> {
            Try<ApplicationServer> catching = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveServerById(snowflake)
                    .execute();
            });

            catching.catching(NotFoundException.class, e -> {
                throw new ServerDoesntExistException(snowflake);
            });

            return catching.unwrap();
        }, bridge.getWorker()).thenApply(server -> {
            Pair<String, String> addressAndNode = Servers.getAddressAndNode(server);

            return new PteroServerImpl(
                bridge,
                server.getIdentifier(),
                addressAndNode.first(),
                addressAndNode.second(),
                server.getName(),
                server.getUUID()
            );
        });
    }

    @Override
    public CompletableFuture<PteroServer> deleteServer(@NotNull PteroServer server) {
        return bridge.getApplication()
            .retrieveServers()
            .cache(true)
            .timeout(10, TimeUnit.SECONDS)
            .takeWhileAsync(1, applicationServer -> applicationServer.getUUID().equals(server.getUUID()))
            .thenApply(collection -> {
                if (collection.isEmpty()) {
                    throw new ServerDoesntExistException(server.getUUID().toString());
                }

                ApplicationServer any = collection.stream()
                    .findAny()
                    .orElseThrow();

                any.getController()
                    .delete(true)
                    .execute();

                return server;
            });
    }

    @Override
    public CompletableFuture<List<PteroServer>> retrieveServersByPage(int page, int size) {
        return bridge.getApplication()
            .retrieveServers()
            .cache(false)
            .skipTo(Math.max(page, 1))
            .limit(size)
            .timeout(10, TimeUnit.SECONDS)
            .takeWhileAsync(1, applicationServer -> {
                ClientServer server = bridge.getClient()
                    .retrieveServerByIdentifier(applicationServer.getIdentifier())
                    .execute();

                Utilization utilization = server.retrieveUtilization()
                    .execute();

                return utilization.getState() == UtilizationState.RUNNING;
            }).thenApply(collection -> {
                if (collection.isEmpty()) {
                    return Collections.emptyList();
                }

                return collection.stream()
                    .map(applicationServer -> {
                        Pair<String, String> addressAndNode = Servers.getAddressAndNode(applicationServer);

                        return new PteroServerImpl(
                            bridge,
                            applicationServer.getIdentifier(),
                            addressAndNode.first(),
                            addressAndNode.second(),
                            applicationServer.getName(),
                            applicationServer.getUUID()
                        );
                    }).collect(Collectors.toList());
            });
    }
}
