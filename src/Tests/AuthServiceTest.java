import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
public class AuthServiceTest {

    private AuthService authService;
    private Map<String, User> users;

    @BeforeEach
    void setUp() {
        users = new HashMap<>();
        authService = new AuthService(users);
    }

    @Test
    void testSuccessfulRegistration() {
        assertTrue(authService.register("newuser", "password123"));
        assertTrue(users.containsKey("newuser"));
    }

    @Test
    void testDuplicateRegistration() {
        authService.register("user1", "pass1");
        assertFalse(authService.register("user1", "pass2"));
    }

    @Test
    void testRegistrationWithInvalidData() {
        assertFalse(authService.register("", "pass"));
        assertFalse(authService.register("user", ""));
        assertFalse(authService.register(null, "pass"));
        assertFalse(authService.register("user", null));
    }

    @Test
    void testSuccessfulLogin() {
        authService.register("testuser", "testpass");
        assertTrue(authService.login("testuser", "testpass"));
        assertTrue(authService.isAuthenticated());
        assertNotNull(authService.getCurrentUser());
    }

    @Test
    void testFailedLogin() {
        authService.register("user", "pass");
        assertFalse(authService.login("user", "wrongpass"));
        assertFalse(authService.login("nonexistent", "pass"));
        assertFalse(authService.isAuthenticated());
    }

    @Test
    void testLogout() {
        authService.register("user", "pass");
        authService.login("user", "pass");
        authService.logout();

        assertFalse(authService.isAuthenticated());
        assertNull(authService.getCurrentUser());
    }
}
