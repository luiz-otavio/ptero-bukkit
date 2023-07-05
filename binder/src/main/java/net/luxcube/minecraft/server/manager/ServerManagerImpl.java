package net.luxcube.minecraft.server.manager;

import com.mattmalec.pterodactyl4j.DataType;
import com.mattmalec.pterodactyl4j.application.entities.ApplicationServer;
import com.mattmalec.pterodactyl4j.application.managers.ServerBuildManager;
import com.mattmalec.pterodactyl4j.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import net.luxcube.minecraft.exception.ServerDoesntExistException;
import net.luxcube.minecraft.logger.PteroLogger;
import net.luxcube.minecraft.manager.ServerManager;
import net.luxcube.minecraft.server.PteroServer;
import net.luxcube.minecraft.util.Try;
import net.luxcube.minecraft.vo.PteroBridgeVO;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author Luiz O. F. Corrêa
 * @since 22/05/2023
 **/
@RequiredArgsConstructor
public class ServerManagerImpl implements ServerManager {

    private static final Consumer EMPTY_CONSUMER = unused -> {
    };

    private final PteroServer pteroServer;
    private final PteroBridgeVO bridge;

    @Override
    public CompletableFuture<Void> setCPU(int cpu) {
        PteroLogger.debug("Setting CPU to %d", cpu);

        return CompletableFuture.supplyAsync(() -> {
            Try<ApplicationServer> catching = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveServerById(pteroServer.getInternalId())
                    .execute();
            });

            catching.catching(NotFoundException.class, e -> {
                throw new ServerDoesntExistException(pteroServer.getIdentifier());
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

        return CompletableFuture.supplyAsync(() -> {
            Try<ApplicationServer> catching = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveServerById(pteroServer.getInternalId())
                    .execute();
            });

            catching.catching(NotFoundException.class, e -> {
                throw new ServerDoesntExistException(pteroServer.getIdentifier());
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

        return CompletableFuture.supplyAsync(() -> {
            Try<ApplicationServer> catching = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveServerById(pteroServer.getInternalId())
                    .execute();
            });

            catching.catching(NotFoundException.class, e -> {
                throw new ServerDoesntExistException(pteroServer.getIdentifier());
            });

            return catching.unwrap();
        }, bridge.getWorker()).thenAccept(applicationServer -> {
            ServerBuildManager buildManager = applicationServer.getBuildManager();

            buildManager.setDisk(disk, DataType.MB);

            buildManager.execute();
        });
    }

    @Override
    public CompletableFuture<Void> bulkResource(int cpu, int ram, int disk) {
        PteroLogger.debug("Setting CPU to %d, RAM to %d and DISK to %d", cpu, ram, disk);

        return CompletableFuture.supplyAsync(() -> {
            Try<ApplicationServer> catching = Try.catching(() -> {
                return bridge.getApplication()
                    .retrieveServerById(pteroServer.getInternalId())
                    .execute();
            });

            catching.catching(NotFoundException.class, e -> {
                throw new ServerDoesntExistException(pteroServer.getIdentifier());
            });

            return catching.unwrap();
        }, bridge.getWorker()).thenAccept(applicationServer -> {
            ServerBuildManager buildManager = applicationServer.getBuildManager();

            buildManager.setCPU(cpu);
            buildManager.setMemory(ram, DataType.MB);
            buildManager.setDisk(disk, DataType.MB);

            buildManager.execute();
        });
    }
}
