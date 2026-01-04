import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
public class StorageServiceIntegrationTest {

    private static final String TEST_FILE = "storage_test.ser";

    @AfterEach
    void tearDown() {
        new File(TEST_FILE).delete();
    }

    @Test
    void testSaveAndLoadUsers() {
        StorageService storageService = new StorageService(TEST_FILE);

        // Create test data
        Map<String, User> originalUsers = new HashMap<>();
        User user1 = new User("user1", "pass1");
        User user2 = new User("user2", "pass2");

        // Add transactions to wallets
        user1.getWallet().addTransaction(new Transaction(Transaction.Type.INCOME, "Salary", 1000.0, ""));
        user1.getWallet().setBudget("Food", 500.0);

        user2.getWallet().addTransaction(new Transaction(Transaction.Type.EXPENSE, "Rent", 800.0, ""));
        user2.getWallet().setBudget("Entertainment", 200.0);

        originalUsers.put("user1", user1);
        originalUsers.put("user2", user2);

        // Save data
        storageService.saveAll(originalUsers);

        // Load data
        Map<String, User> loadedUsers = storageService.loadAll();

        // Verify loaded data
        assertEquals(2, loadedUsers.size());
        assertTrue(loadedUsers.containsKey("user1"));
        assertTrue(loadedUsers.containsKey("user2"));

        User loadedUser1 = loadedUsers.get("user1");
        assertTrue(loadedUser1.checkPassword("pass1"));
        assertEquals(1000.0, loadedUser1.getWallet().totalIncome(), 0.001);
        assertEquals(500.0, loadedUser1.getWallet().getBudget("Food"), 0.001);

        User loadedUser2 = loadedUsers.get("user2");
        assertTrue(loadedUser2.checkPassword("pass2"));
        assertEquals(800.0, loadedUser2.getWallet().totalExpense(), 0.001);
        assertEquals(200.0, loadedUser2.getWallet().getBudget("Entertainment"), 0.001);
    }

    @Test
    void testLoadNonExistentFile() {
        StorageService storageService = new StorageService("non_existent_file.ser");
        Map<String, User> users = storageService.loadAll();

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }
}
