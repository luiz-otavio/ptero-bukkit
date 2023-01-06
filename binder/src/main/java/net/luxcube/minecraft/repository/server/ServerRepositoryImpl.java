package net.luxcube.minecraft.repository.server;

import com.mattmalec.pterodactyl4j.ClientType;
import com.mattmalec.pterodactyl4j.application.entities.ApplicationServer;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
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

import java.util.List;
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
    public CompletableFuture<PteroServer> findServerByName(@NotNull String name) {
        PteroLogger.debug("Searching server by name: %s", name);

        return CompletableFuture.supplyAsync(() -> {
            return bridge.getClient()
                .retrieveServersByName(name, true)
                .execute();
        }, bridge.getWorker()).thenApply(collection -> {
            if (collection.isEmpty()) {
                throw new ServerDoesntExistException(name);
            }

            ClientServer any = collection.stream()
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
        return CompletableFuture.supplyAsync(() -> {
            Try<ApplicationServer> catching = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveServersByName(server.getName(), true)
                    .execute(true)
                    .stream()
                    .findAny()
                    .orElseThrow();
            });

            catching.catching(Exception.class, e -> {
                throw new ServerDoesntExistException(server.getName());
            });

            return catching.unwrap();
        }, bridge.getWorker()).thenApply(applicationServer -> {
            applicationServer.getController()
                .delete(true)
                .execute(true);

            return server;
        });
    }

    @Override
    public CompletableFuture<List<PteroServer>> retrieveServersByPage(int page, int size) {
        return CompletableFuture.supplyAsync(() -> {
            return bridge.getClient()
                .retrieveServers(ClientType.OWNER)
                .skipTo(Math.max(page, 1))
                .limit(size)
                .timeout(10, TimeUnit.SECONDS)
                .execute();
        }).thenApply(clientServers -> {
            return clientServers.stream()
                .map(server -> {
                    Pair<String, String> addressAndNode = Servers.getAddressAndNode(server);

                    return new PteroServerImpl(
                        bridge,
                        server.getIdentifier(),
                        addressAndNode.first(),
                        addressAndNode.second(),
                        server.getName(),
                        server.getUUID()
                    );
                }).collect(Collectors.toList());
        });
    }
}
