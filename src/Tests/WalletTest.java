import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WalletTest {

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet();
    }

    @Test
    void testBasicCategoryOperations() {
        wallet.addCategory("Продукты");
        assertTrue(wallet.hasCategory("Продукты"));
    }

    @Test
    void testBudgetManagement() {
        wallet.setBudget("Продукты", 20000.0);
        assertEquals(20000.0, wallet.getBudget("Продукты"), 0.001);
    }

    @Test
    void testTransactionBasics() {
        wallet.addTransaction(new Transaction(Transaction.Type.INCOME, "Зарплата", 50000.0, ""));
        assertEquals(50000.0, wallet.totalIncome(), 0.001);
    }

    @Test
    void testExpenseCalculation() {
        wallet.addTransaction(new Transaction(Transaction.Type.EXPENSE, "Продукты", 15000.0, ""));
        assertEquals(15000.0, wallet.totalExpense(), 0.001);
    }

    @Test
    void testCategorySpending() {
        wallet.addTransaction(new Transaction(Transaction.Type.EXPENSE, "Продукты", 10000.0, ""));
        assertEquals(10000.0, wallet.spentInCategory("Продукты"), 0.001);
    }

    @Test
    void testSuccessfulCategoryRename() {
        wallet.addCategory("Старая");
        wallet.setBudget("Старая", 10000.0);

        assertTrue(wallet.renameCategory("Старая", "Новая"));
        assertTrue(wallet.hasCategory("Новая"));
        assertEquals(10000.0, wallet.getBudget("Новая"), 0.001);
    }

    @Test
    void testDeleteCategoryWithoutTransactions() {
        wallet.addCategory("Категория");
        assertTrue(wallet.deleteCategory("Категория"));
    }

    @Test
    void testCannotDeleteCategoryWithTransactions() {
        wallet.addTransaction(new Transaction(Transaction.Type.EXPENSE, "Категория", 1000.0, ""));
        assertFalse(wallet.deleteCategory("Категория"));
    }

    @Test
    void testForceDeleteRemovesTransactions() {
        wallet.addTransaction(new Transaction(Transaction.Type.EXPENSE, "Категория", 1000.0, ""));
        int removed = wallet.forceDeleteCategory("Категория");
        assertEquals(1, removed);
        assertFalse(wallet.hasCategory("Категория"));
    }

    @Test
    void testCategoryAggregation() {
        wallet.addTransaction(new Transaction(Transaction.Type.EXPENSE, "Продукты", 5000.0, ""));
        wallet.addTransaction(new Transaction(Transaction.Type.EXPENSE, "Продукты", 3000.0, ""));
        assertEquals(8000.0, wallet.spentInCategory("Продукты"), 0.001);
    }
}