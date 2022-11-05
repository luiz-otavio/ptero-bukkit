package me.luizotavio.minecraft.util;

import com.mattmalec.pterodactyl4j.application.entities.ApplicationAllocation;
import com.mattmalec.pterodactyl4j.application.entities.ApplicationServer;
import me.luizotavio.minecraft.exception.ServerDoesntExistException;
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

            node = allocation.getNode()
                .orElseThrow(() -> new RuntimeException(new ServerDoesntExistException(applicationServer.getName())))
                .getName();
        }

        return new Pair<>(address, node);
    }

}
