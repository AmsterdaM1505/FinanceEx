import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
public class FinanceAppIntegrationTest {

    private static final String TEST_DATA_FILE = "test_data.ser";
    private StorageService storageService;
    private AuthService authService;
    private FinanceService financeService;

    @BeforeEach
    void setUp() {
        // Clean up any existing test file
        new File(TEST_DATA_FILE).delete();

        storageService = new StorageService(TEST_DATA_FILE);
        Map<String, User> users = storageService.loadAll();
        authService = new AuthService(users);
        financeService = new FinanceService(authService);
    }

    @AfterEach
    void tearDown() {
        new File(TEST_DATA_FILE).delete();
    }

    @Test
    void testCompleteUserWorkflow() {
        // Test registration and login
        assertTrue(authService.register("integrationuser", "password"));
        assertTrue(authService.login("integrationuser", "password"));

        // Test category and budget management
        assertEquals("Категория создана: Food", financeService.createCategory("Food"));
        assertEquals("Установлен бюджет Food = 1500,00", financeService.setBudget("Food", 1500.0));

        // Test income and expense operations
        financeService.addIncome("Salary", 5000.0, "Monthly salary");
        financeService.addIncome("Bonus", 1000.0, "Annual bonus");
        financeService.addExpense("Food", 800.0, "Groceries");
        financeService.addExpense("Food", 400.0, "Restaurant");

        // Verify calculations
        User user = authService.getCurrentUser();
        assertEquals(6000.0, user.getWallet().totalIncome(), 0.001);
        assertEquals(1200.0, user.getWallet().totalExpense(), 0.001);
        assertEquals(1200.0, user.getWallet().spentInCategory("Food"), 0.001);

        // Test summary
        String summary = financeService.summary();
        assertTrue(summary.contains("6000,00"));
        assertTrue(summary.contains("1200,00"));
        assertTrue(summary.contains("Food"));

        // Test data persistence
        storageService.saveAll(authService.getUsersMap());

        // Reload and verify data
        Map<String, User> reloadedUsers = storageService.loadAll();
        assertTrue(reloadedUsers.containsKey("integrationuser"));
        User reloadedUser = reloadedUsers.get("integrationuser");
        assertEquals(6000.0, reloadedUser.getWallet().totalIncome(), 0.001);
    }

    @Test
    void testMultiUserTransferScenario() {
        // Setup users
        authService.register("sender", "pass1");
        authService.register("receiver", "pass2");

        // Sender operations
        authService.login("sender", "pass1");
        financeService.addIncome("Salary", 3000.0, "");
        financeService.setBudget("Transfer", 1000.0);

        // Perform transfer
        String transferResult = financeService.transfer("receiver", 500.0, "Test");
        assertTrue(transferResult.contains("Перевод выполнен:"));

        // Verify sender state
        User sender = authService.getCurrentUser();
        assertEquals(3000.0, sender.getWallet().totalIncome(), 0.001);
        assertEquals(500.0, sender.getWallet().totalExpense(), 0.001);

        // Switch to receiver
        authService.logout();
        authService.login("receiver", "pass2");

        // Verify receiver state
        User receiver = authService.getCurrentUser();
        assertEquals(500.0, receiver.getWallet().totalIncome(), 0.001);
        assertEquals(0.0, receiver.getWallet().totalExpense(), 0.001);
    }

    @Test
    void testBudgetAlertsAndWarnings() {
        authService.register("alertuser", "pass");
        authService.login("alertuser", "pass");

        financeService.createCategory("TestCategory");
        financeService.setBudget("TestCategory", 1000.0);
        financeService.addIncome("Source", 2000.0, "");

        // Test 80% budget warning
        String warningResult = financeService.addExpense("TestCategory", 850.0, "Test");
        assertTrue(warningResult.contains("Предупреждение: вы использовали >=80% бюджета"));

        // Test budget exceeded alert
        String alertResult = financeService.addExpense("TestCategory", 200.0, "Test");
        assertTrue(alertResult.contains("Бюджет по категории 'TestCategory' превышен"));

        // Test overall expense alert
        String overallAlertResult = financeService.addExpense("TestCategory", 1000.0, "Test");
        assertTrue(overallAlertResult.contains("Оповещение: общие расходы превысили доходы"));
    }
}
