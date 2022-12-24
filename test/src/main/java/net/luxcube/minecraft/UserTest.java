package net.luxcube.minecraft;

import net.luxcube.minecraft.PteroManager;
import net.luxcube.minecraft.PteroManagerImpl;
import net.luxcube.minecraft.user.PteroUser;
import net.luxcube.minecraft.util.Users;
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
    static UUID uuid;

    @BeforeAll
    public static void setup() {
        pteroManager = new PteroManagerImpl(
            "ptla_NZuGwOqmNT8BpCH4hT2LfYBkftWvE989aYhoDixZe2q",
            "ptlc_fPK6UAWJwLuSbSteEng1dIhc2p4G5pVImrA0k9xuROH",
            "http://5.249.162.105",
            4
        );

        uuid = UUID.randomUUID();
    }

    @Test
    public void createUser() {
        PteroUser user = pteroManager.getFactory()
            .createUser(
                uuid,
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
    public void findUserByUUID() {
        UUID targetUUID = UUID.randomUUID();

        System.out.println("Users.fromShort() = " + Users.fromShort(targetUUID));

        PteroUser user = pteroManager.getFactory()
            .createUser(
                targetUUID,
                "luxcube-user",
                "123456",
                null
            ).exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            }).join();

        assertNotNull(user, "User is null");

        System.out.println("user.getId() = " + user.getId());

        PteroUser targetUser = pteroManager.getUserRepository()
            .findUserByUUID(targetUUID)
            .exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            }).join();

        assertNotNull(targetUser, "Target user is null");

        System.out.println("targetUser.getId() = " + targetUser.getId());
    }

    @Test
    public void deleteUser() {
        PteroUser pteroUser = pteroManager.getUserRepository()
            .findUserByUsername("luiz-otavio")
            .exceptionally(throwable -> null)
            .join();

        assertNotNull(pteroUser, "User not found");

        pteroManager.getUserRepository()
            .deleteUser(pteroUser)
            .exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            }).join();
    }
}
