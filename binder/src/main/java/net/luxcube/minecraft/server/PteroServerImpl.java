package net.luxcube.minecraft.server;

import com.mattmalec.pterodactyl4j.Permission;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.client.entities.ClientSubuser;
import com.mattmalec.pterodactyl4j.client.entities.Utilization;
import com.mattmalec.pterodactyl4j.exceptions.NotFoundException;
import com.mattmalec.pterodactyl4j.exceptions.PteroException;
import net.luxcube.minecraft.exception.ServerDoesntExistException;
import net.luxcube.minecraft.logger.PteroLogger;
import net.luxcube.minecraft.manager.ServerManager;
import net.luxcube.minecraft.server.manager.ServerManagerImpl;
import net.luxcube.minecraft.server.status.StatusType;
import net.luxcube.minecraft.server.usage.ServerUsage;
import net.luxcube.minecraft.server.usage.ServerUsageImpl;
import net.luxcube.minecraft.user.PteroUser;
import net.luxcube.minecraft.util.Pair;
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
    private final String internalId;
    private final String address;
    private final String node;
    private String name;

    private final UUID uuid;

    private final ServerManager manager;

    public PteroServerImpl(
        @NotNull PteroBridgeVO bridge,
        @NotNull String identifier,
        @NotNull String internalId,
        @NotNull String address,
        @NotNull String node,
        @NotNull String name,
        @NotNull UUID uuid
    ) {
        this.bridge = bridge;
        this.identifier = identifier;
        this.address = address;
        this.internalId = internalId;
        this.node = node;
        this.name = name;
        this.uuid = uuid;

        this.manager = new ServerManagerImpl(this, bridge);
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public @NotNull String getInternalId() {
        return internalId;
    }

    @Override
    public @NotNull String getName() {
        return name;
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
    public @NotNull ServerManager getManager() {
        return manager;
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
    public CompletableFuture<Void> changeName(@NotNull String name) {
        PteroLogger.debug("Changing name of server %s to %s", identifier, name);

        return CompletableFuture.supplyAsync(() -> {
            Try<ClientServer> catching = Try.catching(() -> {
                return bridge.getClient()
                    .retrieveServerByIdentifier(identifier)
                    .execute();
            });

            catching.catching(NotFoundException.class, e -> {
                throw new ServerDoesntExistException(identifier);
            });

            return catching.unwrap();
        }, bridge.getWorker()).thenAccept(clientServer -> {
            clientServer.getManager()
                .setName(name)
                .execute();

            this.name = name;
        });
    }

    @Override
    public CompletableFuture<Void> allow(@NotNull PteroUser pteroUser) {
        PteroLogger.debug("Allowing user %s to access server %s", pteroUser.getId(), identifier);

        return CompletableFuture.supplyAsync(() -> {
            return bridge.getClient()
                .retrieveServerByIdentifier(identifier)
                .execute();
        }, bridge.getWorker()).thenApply(clientServer -> {
            Try<ClientSubuser> catching = Try.catching(() -> {
                return clientServer.retrieveSubuser(pteroUser.getUniqueId())
                    .execute();
            });

            catching.catching(PteroException.class, e -> {
                throw new ServerDoesntExistException(identifier);
            });

            return new Pair<>(clientServer, catching.unwrap());
        }).thenAccept(pair -> {
            ClientSubuser user = pair.second();

            if (user.hasPermission(Permission.CONTROL_PERMISSIONS)) {
                PteroLogger.debug("User %s already has permission to access server %s", pteroUser.getId(), identifier);
                return;
            }

            Permission[] permissions = new Permission[user.getPermissions().size() + Permission.CONTROL_PERMISSIONS.length];

            // Clone permissions
            System.arraycopy(user.getPermissions().toArray(), 0, permissions, 0, user.getPermissions().size());

            // Add control permissions
            System.arraycopy(Permission.CONTROL_PERMISSIONS, 0, permissions, user.getPermissions().size(), Permission.CONTROL_PERMISSIONS.length);

            ClientServer server = pair.first();
            server.getSubuserManager()
                .editUser(user)
                .setPermissions(permissions)
                .execute(true);

            PteroLogger.debug("User %s now has permission to access server %s", pteroUser.getId(), identifier);
        });
    }

    @Override
    public CompletableFuture<Void> disallow(@NotNull PteroUser pteroUser) {
        PteroLogger.debug("Disallowing user %s to access server %s", pteroUser.getId(), identifier);

        return CompletableFuture.supplyAsync(() -> {
            return bridge.getClient()
                .retrieveServerByIdentifier(identifier)
                .execute();
        }, bridge.getWorker()).thenApply(clientServer -> {
            Try<ClientSubuser> catching = Try.catching(() -> {
                return clientServer.retrieveSubuser(pteroUser.getUniqueId())
                    .execute();
            });

            catching.catching(PteroException.class, e -> {
                throw new ServerDoesntExistException(identifier);
            });

            return new Pair<>(clientServer, catching.unwrap());
        }).thenAccept(pair -> {
            ClientSubuser user = pair.second();

            if (!user.hasPermission(Permission.CONTROL_PERMISSIONS)) {
                PteroLogger.debug("User %s already doesn't have permission to access server %s", pteroUser.getId(), identifier);
                return;
            }

            Permission[] permissions = user.getPermissions()
                .toArray(new Permission[0]);

            // Remove control permissions
            for (Permission permission : Permission.CONTROL_PERMISSIONS) {
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i] == permission) {
                        permissions[i] = null;
                        break;
                    }
                }
            }

            ClientServer server = pair.first();
            server.getSubuserManager()
                .editUser(user)
                .setPermissions(permissions)
                .execute(true);

            PteroLogger.debug("User %s now doesn't have permission to access server %s", pteroUser.getId(), identifier);
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
                .execute(true);
        }, bridge.getWorker());
    }

    @Override
    public CompletableFuture<Boolean> hasPermission(@NotNull PteroUser pteroUser) {
        PteroLogger.debug("Checking if user %s has permission to access server %s", pteroUser.getId(), identifier);

        return CompletableFuture.supplyAsync(() -> {
            return bridge.getClient()
                .retrieveServerByIdentifier(identifier)
                .timeout(5, TimeUnit.SECONDS)
                .execute();
        }).thenApply(clientServer -> {
            return clientServer.getSubusers()
                .stream()
                .anyMatch(subUser -> subUser.getEmail().equals(pteroUser.getEmail()));
        });
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
                .execute(true);
        }, bridge.getWorker());
    }
}
