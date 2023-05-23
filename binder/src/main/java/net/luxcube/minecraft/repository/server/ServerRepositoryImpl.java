package net.luxcube.minecraft.repository.server;

import com.mattmalec.pterodactyl4j.ClientType;
import com.mattmalec.pterodactyl4j.application.entities.ApplicationServer;
import com.mattmalec.pterodactyl4j.client.entities.ClientAllocation;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.exceptions.NotFoundException;
import com.mattmalec.pterodactyl4j.requests.PaginationAction;
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
import java.util.Vector;
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
                any.getId(),
                addressAndNode.first(),
                addressAndNode.second(),
                any.getName(),
                any.getUUID()
            );
        });
    }

    @Override
    public CompletableFuture<PteroServer> findServerBySnowflake(@NotNull String snowflake) {
        PteroLogger.debug("Searching server by snowflake: %s", snowflake);

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
                server.getId(),
                addressAndNode.first(),
                addressAndNode.second(),
                server.getName(),
                server.getUUID()
            );
        });
    }

    @Override
    public CompletableFuture<PteroServer> deleteServer(@NotNull PteroServer server) {
        PteroLogger.debug("Deleting server: %s", server.getName());

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
        PteroLogger.debug("Retrieving servers by page: %d, size: %d", page, size);

        return CompletableFuture.supplyAsync(() -> {
            return bridge.getApplication()
                .retrieveServers()
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
                        server.getId(),
                        addressAndNode.first(),
                        addressAndNode.second(),
                        server.getName(),
                        server.getUUID()
                    );
                }).collect(Collectors.toList());
        });
    }

    @Override
    public CompletableFuture<PteroServer> retrieveServerByDomain(@NotNull String domain) {
        PteroLogger.debug("Retrieving server by domain: %s", domain);

        return CompletableFuture.supplyAsync(() -> {
            List<ClientServer> clientServers = new Vector<>();

            int currentPage = 1,
                maxPage = 1;

            PaginationAction<ClientServer> pteroAction = bridge.getClient()
                .retrieveServers(ClientType.OWNER);
            do {
                List<ClientServer> servers = pteroAction.execute();

                if (pteroAction.getTotalPages() != maxPage) {
                    maxPage = pteroAction.getTotalPages();
                }

                clientServers.addAll(servers);

                if (currentPage < maxPage) {
                    currentPage++;
                } else {
                    break;
                }
            } while (currentPage <= maxPage);

            return clientServers;
        }).thenApply(clientServers -> {
            ClientServer clientServer = null;
            for (ClientServer targetServer : clientServers) {
                ClientAllocation clientAllocation = targetServer.getPrimaryAllocation();

                if (clientAllocation == null) {
                    continue;
                }

                String note;
                try {
                    note = clientAllocation.getNotes();
                } catch (Exception exception) {
                    continue;
                }

                if (note == null || note.isEmpty()) {
                    continue;
                }

                if (note.equals(domain)) {
                    clientServer = targetServer;
                    break;
                }
            }

            if (clientServer == null) {
                throw new ServerDoesntExistException(domain);
            }

            Pair<String, String> addressAndNode = Servers.getAddressAndNode(clientServer);

            return new PteroServerImpl(
                bridge,
                clientServer.getIdentifier(),
                clientServer.getInternalId(),
                addressAndNode.first(),
                addressAndNode.second(),
                clientServer.getName(),
                clientServer.getUUID()
            );
        });
    }
}
