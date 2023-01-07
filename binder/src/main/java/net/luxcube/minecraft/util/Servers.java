package net.luxcube.minecraft.util;

import com.mattmalec.pterodactyl4j.application.entities.ApplicationAllocation;
import com.mattmalec.pterodactyl4j.application.entities.ApplicationServer;
import com.mattmalec.pterodactyl4j.client.entities.ClientAllocation;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.entities.Allocation;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class Servers {

    private static final String LOCAL_IP = "127.0.0.1";
    private static final String PUBLIC_IP = "0.0.0.0";

    @Blocking
    public static Pair<String, String> getAddressAndNode(@NotNull ApplicationServer applicationServer) {
        ApplicationAllocation allocation = applicationServer.retrieveDefaultAllocation()
            .timeout(5, TimeUnit.SECONDS)
            .execute();

        String node = applicationServer.retrieveNode()
            .timeout(5, TimeUnit.SECONDS)
            .execute()
            .getName();

        return new Pair<>(ensureAddress(allocation), node);
    }

    @Blocking
    public static Pair<String, String> getAddressAndNode(@NotNull ClientServer clientServer) {
        ClientAllocation allocation = clientServer.getPrimaryAllocation();

        return new Pair<>(
            ensureAddress(allocation),
            clientServer.getNode()
        );
    }

    private static String ensureAddress(@NotNull Allocation allocation) {
        String address = allocation.getIP(),
            port = allocation.getPort();

        if (address.equalsIgnoreCase(LOCAL_IP) || address.equalsIgnoreCase(PUBLIC_IP)) {
            address = allocation.getAlias();
        }

        return String.format("%s:%s", address, port);
    }


}
