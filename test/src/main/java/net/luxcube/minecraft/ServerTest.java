package net.luxcube.minecraft;

import net.luxcube.minecraft.server.PteroServer;
import net.luxcube.minecraft.user.PteroUser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Luiz O. F. Corrêa
 * @since 05/11/2022
 **/
public class ServerTest {

    static PteroManager pteroManager;

    @BeforeAll
    public static void setup() {
        pteroManager = new PteroManagerImpl(
            "ptla_NZuGwOqmNT8BpCH4hT2LfYBkftWvE989aYhoDixZe2q",
            "ptlc_wL9JEOo7b4pVUBRQFhpth8BR0fTIe7LEOd2y4wRjPpY",
            "http://5.249.162.105",
            4
        );
    }

    @Test
    public void createUser() {
        PteroUser user = pteroManager.getFactory()
            .createUser(
                UUID.randomUUID(),
                "luiz-otavio",
                "123456",
                null
            ).exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            }).join();

        assertNotNull(user, "User is null");

        System.out.println("user.getId() = " + user.getId());
    }

    @Test
    public void createServer() {
        PteroUser pteroUser = pteroManager.getUserRepository()
            .findUserByUsername("luiz-otavio")
            .exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            }).join();

        assertNotNull(pteroUser, "User not found");

        PteroServer server = pteroManager.getFactory()
            .createServer(
                "LuxCube's Server",
                pteroUser,
                "Paper",
                "ghcr.io/pterodactyl/yolks:java_8",
                "java -Xms128M -XX:MaxRAMPercentage=95.0 -Dterminal.jline=false -Dterminal.ansi=true -jar {{SERVER_JARFILE}}",
                512,
                512,
                0
            ).exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            }).join();

        assertNotNull(server, "Server is null");

        System.out.println("server.getIdentifier() = " + server.getIdentifier());
    }

    @Test
    public void starting() {
        PteroServer server = pteroManager.getServerRepository()
            .findServerByName("LuxCube's Server")
            .exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            }).join();

        assertNotNull(server, "Server not found");

        server.start().exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        }).join();
    }

    @Test
    public void stopping() {
        PteroServer server = pteroManager.getServerRepository()
            .findServerByName("LuxCube's Server")
            .exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            }).join();

        assertNotNull(server, "Server not found");

        server.stop().exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        }).join();
    }

    @Test
    public void deleteServer() {
        PteroServer server = pteroManager.getServerRepository()
            .findServerByName("LuxCube's Server")
            .exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            }).join();

        assertNotNull(server, "Server not found");

        pteroManager.getServerRepository()
            .deleteServer(server)
            .exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            }).join();
    }

    @Test
    public void deleteUser() {
        PteroUser pteroUser = pteroManager.getUserRepository()
            .findUserByUsername("luiz-otavio")
            .exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            }).join();

        assertNotNull(pteroUser, "User not found");

        pteroManager.getUserRepository()
            .deleteUser(pteroUser)
            .exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            }).join();
    }

}
