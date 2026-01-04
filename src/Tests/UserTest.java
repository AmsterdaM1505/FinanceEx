import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class UserTest {

    @Test
    void testUserCreation() {
        User user = new User("testuser", "password123");

        assertEquals("testuser", user.getLogin());
        assertNotNull(user.getWallet());
        assertTrue(user.checkPassword("password123"));
        assertFalse(user.checkPassword("wrongpassword"));
    }

    @Test
    void testPasswordHashing() {
        User user1 = new User("user1", "password");
        User user2 = new User("user2", "password");

        // Same password should have same hash
        assertTrue(user1.checkPassword("password"));
        assertTrue(user2.checkPassword("password"));
    }

    @Test
    void testWalletManagement() {
        User user = new User("test", "pass");
        Wallet newWallet = new Wallet();
        user.setWallet(newWallet);

        assertSame(newWallet, user.getWallet());
    }
}
