import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Wallet implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<Transaction> transactions = new ArrayList<>();
    private final Map<String, Double> budgets = new HashMap<>(); // category -> budget
    private final Set<String> categories = new HashSet<>();

    public void addCategory(String category) {
        categories.add(category);
    }
    public boolean hasCategory(String category) {
        return categories.contains(category); 
    }
    public Set<String> getCategories() {
        return Collections.unmodifiableSet(categories); 
    }

    public void setBudget(String category, double amount) {
        budgets.put(category, amount);
        categories.add(category);
    }
    public Double getBudget(String category) {
        return budgets.get(category); 
    }
    public Map<String, Double> getBudgets() {
        return Collections.unmodifiableMap(budgets); 
    }

    public void addTransaction(Transaction tx) {
        transactions.add(tx);
        categories.add(tx.getCategory());
    }
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions); 
    }

    public double totalIncome() {
        return transactions.stream().filter(t->t.getType()==Transaction.Type.INCOME).mapToDouble(Transaction::getAmount).sum();
    }
    public double totalExpense() {
        return transactions.stream().filter(t->t.getType()==Transaction.Type.EXPENSE).mapToDouble(Transaction::getAmount).sum();
    }
    public double totalByCategory(String category, Transaction.Type type) {
        return transactions.stream()
                .filter(t->t.getType()==type && t.getCategory().equalsIgnoreCase(category))
                .mapToDouble(Transaction::getAmount).sum();
    }
    public Map<String, Double> totalsByCategory(Transaction.Type type) {
        Map<String, Double> res = new HashMap<>();
        for(Transaction t: transactions) {
            if(t.getType()==type) {
                res.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        return res;
    }

    public double spentInCategory(String category) {
        return totalByCategory(category, Transaction.Type.EXPENSE);
    }

    public boolean renameCategory(String oldCategory, String newCategory) {
        if (oldCategory == null || newCategory == null ||
                oldCategory.trim().isEmpty() || newCategory.trim().isEmpty()) {
            return false;
        }

        if (!categories.contains(oldCategory)) {
            return false; // Старая категория не существует
        }

        if (categories.contains(newCategory) && !oldCategory.equals(newCategory)) {
            return false; // Новая категория уже существует
        }

        // Обновляем название категории в Set категорий
        categories.remove(oldCategory);
        categories.add(newCategory);

        // Обновляем бюджеты
        Double budget = budgets.get(oldCategory);
        if (budget != null) {
            budgets.remove(oldCategory);
            budgets.put(newCategory, budget);
        }

        // Обновляем транзакции (создаем новые с обновленной категорией)
        List<Transaction> updatedTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getCategory().equals(oldCategory)) {
                // Создаем новую транзакцию с обновленной категорией
                Transaction updatedTransaction = new Transaction(
                        transaction.getType(),
                        newCategory,
                        transaction.getAmount(),
                        transaction.getNote()
                );
                updatedTransactions.add(updatedTransaction);
            } else {
                updatedTransactions.add(transaction);
            }
        }

        // Заменяем старый список транзакций на обновленный
        transactions.clear();
        transactions.addAll(updatedTransactions);

        return true;
    }

    public boolean deleteCategory(String category) {
        if (category == null || category.trim().isEmpty() || !categories.contains(category)) {
            return false;
        }

        // Проверяем, есть ли транзакции с этой категорией
        boolean hasTransactions = transactions.stream()
                .anyMatch(t -> t.getCategory().equals(category));

        if (hasTransactions) {
            return false; // Нельзя удалить категорию с транзакциями
        }

        // Удаляем категорию из всех мест
        categories.remove(category);
        budgets.remove(category);

        return true;
    }
    public int forceDeleteCategory(String category) {
        if (category == null || category.trim().isEmpty() || !categories.contains(category)) {
            return -1;
        }

        // Подсчитываем и удаляем связанные транзакции
        int removedTransactions = (int) transactions.stream()
                .filter(t -> t.getCategory().equals(category))
                .count();

        transactions.removeIf(t -> t.getCategory().equals(category));

        // Удаляем категорию
        categories.remove(category);
        budgets.remove(category);

        return removedTransactions;
    }

    // Добавляем геттер для удобства
    public List<Transaction> getTransactionsByCategory(String category) {
        return transactions.stream()
                .filter(t -> t.getCategory().equals(category))
                .collect(Collectors.toList());
    }
}
