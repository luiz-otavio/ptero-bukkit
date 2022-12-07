package net.luxcube.minecraft.util;

import com.mattmalec.pterodactyl4j.application.entities.ApplicationAllocation;
import com.mattmalec.pterodactyl4j.application.entities.ApplicationServer;
import com.mattmalec.pterodactyl4j.client.entities.ClientAllocation;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * @author Luiz O. F. Corrêa
 * @since 02/11/2022
 **/
public class Servers {

    @Blocking
    public static Pair<String, String> getAddressAndNode(@NotNull ApplicationServer applicationServer) {
        ApplicationAllocation allocation = applicationServer.retrieveDefaultAllocation()
            .timeout(5, TimeUnit.SECONDS)
            .execute();

        String address = null,
            node = null;
        if (allocation == null) {
            address = "";
            node = "N/A";
        } else {
            address = allocation.getFullAddress();

            node = applicationServer.retrieveNode()
                .timeout(5, TimeUnit.SECONDS)
                .execute()
                .getName();
        }

        return new Pair<>(address, node);
    }

    @Blocking
    public static Pair<String, String> getAddressAndNode(@NotNull ClientServer clientServer) {
        ClientAllocation allocation = clientServer.getPrimaryAllocation();

        return new Pair<>(
            allocation.getFullAddress(),
            clientServer.getNode()
        );
    }

}
