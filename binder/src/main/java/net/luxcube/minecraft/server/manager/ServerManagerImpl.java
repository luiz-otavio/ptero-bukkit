package net.luxcube.minecraft.server.manager;

import com.mattmalec.pterodactyl4j.DataType;
import com.mattmalec.pterodactyl4j.application.entities.ApplicationAllocation;
import com.mattmalec.pterodactyl4j.application.entities.ApplicationServer;
import com.mattmalec.pterodactyl4j.application.entities.Node;
import com.mattmalec.pterodactyl4j.application.entities.impl.ApplicationAllocationManagerImpl;
import com.mattmalec.pterodactyl4j.application.managers.ServerBuildManager;
import com.mattmalec.pterodactyl4j.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import net.luxcube.minecraft.exception.ServerDoesntExistException;
import net.luxcube.minecraft.logger.PteroLogger;
import net.luxcube.minecraft.manager.ServerManager;
import net.luxcube.minecraft.server.PteroServer;
import net.luxcube.minecraft.util.Try;
import net.luxcube.minecraft.vo.PteroBridgeVO;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Luiz O. F. Corrêa
 * @since 22/05/2023
 **/
@RequiredArgsConstructor
public class ServerManagerImpl implements ServerManager {

    private final PteroServer pteroServer;
    private final PteroBridgeVO bridge;

    @Override
    public CompletableFuture<Void> setCPU(int cpu) {
        PteroLogger.debug("Setting CPU to %d", cpu);

        String identifier = pteroServer.getIdentifier();

        return CompletableFuture.supplyAsync(() -> {
            Try<ApplicationServer> catching = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveServersByName(pteroServer.getName(), true)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute()
                    .stream()
                    .findAny()
                    .orElseThrow(() -> new NotFoundException("Server not found"));
            });

            catching.catching(NotFoundException.class, e -> {
                throw new ServerDoesntExistException(identifier);
            });

            return catching.unwrap();
        }, bridge.getWorker()).thenAccept(applicationServer -> {
            ServerBuildManager buildManager = applicationServer.getBuildManager();

            buildManager.setCPU(cpu);

            buildManager.execute();
        });
    }

    @Override
    public CompletableFuture<Void> setRam(int ram) {
        PteroLogger.debug("Setting RAM to %d", ram);

        String identifier = pteroServer.getIdentifier();

        return CompletableFuture.supplyAsync(() -> {
            Try<ApplicationServer> catching = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveServersByName(pteroServer.getName(), true)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute()
                    .stream()
                    .findAny()
                    .orElseThrow(() -> new NotFoundException("Server not found"));
            });

            catching.catching(NotFoundException.class, e -> {
                throw new ServerDoesntExistException(identifier);
            });

            return catching.unwrap();
        }, bridge.getWorker()).thenAccept(applicationServer -> {
            ServerBuildManager buildManager = applicationServer.getBuildManager();

            buildManager.setMemory(ram, DataType.MB);

            buildManager.execute();
        });
    }

    @Override
    public CompletableFuture<Void> setDisk(int disk) {
        PteroLogger.debug("Setting DISK to %d", disk);

        String identifier = pteroServer.getIdentifier();

        return CompletableFuture.supplyAsync(() -> {
            Try<ApplicationServer> catching = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveServersByName(pteroServer.getName(), true)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute()
                    .stream()
                    .findAny()
                    .orElseThrow(() -> new NotFoundException("Server not found"));
            });

            catching.catching(NotFoundException.class, e -> {
                throw new ServerDoesntExistException(identifier);
            });

            return catching.unwrap();
        }, bridge.getWorker()).thenAccept(applicationServer -> {
            ServerBuildManager buildManager = applicationServer.getBuildManager();

            buildManager.setDisk(disk, DataType.MB);

            buildManager.execute();
        });
    }

    @Override
    public CompletableFuture<Boolean> setDomain(@NotNull String domain) {
        PteroLogger.debug("Setting domain to %s", domain);

        String identifier = pteroServer.getIdentifier();

        return CompletableFuture.supplyAsync(() -> {
            Try<ApplicationServer> catching = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveServersByName(pteroServer.getName(), true)
                    .timeout(5, TimeUnit.SECONDS)
                    .execute()
                    .stream()
                    .findAny()
                    .orElseThrow(() -> new NotFoundException("Server not found"));
            });

            catching.catching(NotFoundException.class, e -> {
                throw new ServerDoesntExistException(identifier);
            });

            return catching.unwrap();
        }, bridge.getWorker()).thenApply(applicationServer -> {
            ApplicationAllocation allocation = applicationServer.retrieveDefaultAllocation()
                .execute();

            if (allocation == null) {
                return false;
            }

            Node node = allocation.getNode()
                .orElseThrow(() -> new ServerDoesntExistException(identifier));

            if (!(node.getAllocationManager() instanceof ApplicationAllocationManagerImpl allocationManager)) {
                throw new ServerDoesntExistException(identifier);
            }

            allocationManager.editAllocation(allocation)
                .setAlias(domain)
                .execute();

            return true;
        });
    }
}
