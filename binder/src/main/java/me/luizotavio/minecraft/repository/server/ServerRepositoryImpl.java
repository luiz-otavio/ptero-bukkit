package me.luizotavio.minecraft.repository.server;

import com.mattmalec.pterodactyl4j.application.entities.ApplicationServer;
import com.mattmalec.pterodactyl4j.exceptions.NotFoundException;
import me.luizotavio.minecraft.exception.ServerDoesntExistException;
import me.luizotavio.minecraft.server.PteroServer;
import me.luizotavio.minecraft.server.PteroServerImpl;
import me.luizotavio.minecraft.util.Pair;
import me.luizotavio.minecraft.util.Servers;
import me.luizotavio.minecraft.util.Try;
import me.luizotavio.minecraft.vo.PteroBridgeVO;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
                    any.getUUID()
                );
            });

    }

    @Override
    public CompletableFuture<PteroServer> findServerByName(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> {
            return bridge.getApplication()
                .retrieveServersByName(name, true)
                .execute();
        }).thenApply(collection -> {
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
}
