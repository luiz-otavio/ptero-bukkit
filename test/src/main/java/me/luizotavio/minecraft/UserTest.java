package me.luizotavio.minecraft;

import me.luizotavio.minecraft.user.PteroUser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Luiz O. F. Corrêa
 * @since 05/11/2022
 **/
public class UserTest {

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
