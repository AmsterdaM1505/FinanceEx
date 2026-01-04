import java.util.*;

public class FinanceService {
    private final AuthService auth;
    private final Map<String, User> allUsers;

    public FinanceService(AuthService auth) {
        this.auth = auth;
        this.allUsers = auth.getUsersMap();
    }

    // дальше по пользователю будут взаимодействия с кошельком для хранения финансов
    // Wallet w = auth.getCurrentUser().getWallet(); - получаем кошелек текущего пользователя и дальше с ним работаем

    // еще в каждом блоке есть оповещения разных типов
    // предупреждение и оповещение
    public String createCategory(String name) { // логика добавления категории
        if (!auth.isAuthenticated()) {
            return "Нужна авторизация"; // тут и в подобных местах проверка корректности данных
        }
        if (name==null || name.trim().isEmpty()) {
            return "Имя категории пустое";
        }
        Wallet w = auth.getCurrentUser().getWallet(); // взаимодействуем с кошельком по конкретному пользователю
        w.addCategory(name);
        return "Категория создана: " + name;
    }

    public String setBudget(String category, double amount) { // логика добавления бюджета по категории
        if (!auth.isAuthenticated()) {
            return "Нужна авторизация";
        }
        if (amount < 0) {
            return "Бюджет не может быть отрицательным";
        }
        Wallet w = auth.getCurrentUser().getWallet();
        w.setBudget(category, amount);
        return String.format("Установлен бюджет %s = %.2f", category, amount);
    }

    public String addIncome(String category, double amount, String note) { // логика добавления дохода
        if (!auth.isAuthenticated()) {
            return "Нужна авторизация";
        }
        if (amount <= 0) {
            return "Сумма должна быть > 0";
        }
        Wallet w = auth.getCurrentUser().getWallet();
        Transaction tx = new Transaction(Transaction.Type.INCOME, category, amount, note);
        w.addTransaction(tx);
        return "Доход добавлен: " + tx;
    }

    public String addExpense(String category, double amount, String note) {
        if (!auth.isAuthenticated()) {
            return "Нужна авторизация";
        }
        if (amount <= 0) {
            return "Сумма должна быть > 0";
        }
        Wallet w = auth.getCurrentUser().getWallet();
        if (!w.hasCategory(category)) {
            return "Категория не найдена. Создайте её или используйте существующую.";
        }
        Transaction tx = new Transaction(Transaction.Type.EXPENSE, category, amount, note);
        w.addTransaction(tx);
        StringBuilder sb = new StringBuilder("Расход добавлен: " + tx);
        Double budget = w.getBudget(category);
        if (budget != null) {
            double spent = w.spentInCategory(category);
            double remaining = budget - spent;
            if (remaining < 0) {
                sb.append(String.format("\nБюджет по категории '%s' превышен на %.2f", category, -remaining));
            } else if (remaining <= 0.2 * budget) { // предусмотрено расширенное оповещение
                sb.append(String.format("\nПредупреждение: вы использовали >=80%% бюджета категории '%s' (остаток %.2f).", category, remaining));
            }
        }
        if (w.totalExpense() > w.totalIncome()) {
            sb.append("\nОповещение: общие расходы превысили доходы.");
        }
        return sb.toString();
    }

    public String transfer(String toLogin, double amount, String note) {
        if (!auth.isAuthenticated()) {
            return "Нужна авторизация";
        }
        if (amount <= 0) {
            return "Сумма должна быть > 0";
        }
        String fromLogin = auth.getCurrentUser().getLogin();
        if (fromLogin.equals(toLogin)) {
            return "Нельзя переводить самому себе";
        }
        User to = allUsers.get(toLogin);
        if (to == null) {
            return "Получатель не найден";
        }
        Wallet wFrom = auth.getCurrentUser().getWallet();
        Wallet wTo = to.getWallet();
        // фиксируем расход у отправителя и доход у получателя
        Transaction txOut = new Transaction(Transaction.Type.EXPENSE, "transfer:" + toLogin, amount, "transfer to " + toLogin + (note==null?"":"; "+note));
        Transaction txIn = new Transaction(Transaction.Type.INCOME, "transfer-from:" + fromLogin, amount, "transfer from " + fromLogin + (note==null?"":"; "+note));
        wFrom.addTransaction(txOut);
        wTo.addTransaction(txIn);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Перевод выполнен: %s -> %s : %.2f", fromLogin, toLogin, amount));
        String category = "transfer:" + toLogin;
        Double budget = wFrom.getBudget(category);
        if (budget != null) {
            double rem = budget - wFrom.spentInCategory(category);
            if (rem < 0) sb.append(String.format("\nВнимание! Бюджет по категории '%s' превышен на %.2f", category, -rem));
        }
        if (wFrom.totalExpense() > wFrom.totalIncome()) {
            sb.append("\nОповещение: общие расходы превысили доходы у отправителя.");
        }
        return sb.toString();
    }

    public String summary() {
        if (!auth.isAuthenticated()) {
            return "Нужна авторизация";
        }
        Wallet w = auth.getCurrentUser().getWallet();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Пользователь: %s\n", auth.getCurrentUser().getLogin()));
        sb.append(String.format("Общий доход: %.2f\n", w.totalIncome()));
        Map<String, Double> incomes = w.totalsByCategory(Transaction.Type.INCOME);
        if (!incomes.isEmpty()) {
            sb.append("Доходы по категориям:\n");
            incomes.forEach((k,v)-> sb.append(String.format("  %s: %.2f\n", k, v)));
        }
        sb.append(String.format("Общие расходы: %.2f\n", w.totalExpense()));
        Map<String, Double> expenses = w.totalsByCategory(Transaction.Type.EXPENSE);
        if (!expenses.isEmpty()) {
            sb.append("Расходы по категориям:\n");
            expenses.forEach((k,v)-> sb.append(String.format("  %s: %.2f\n", k, v)));
        }
        sb.append("Бюджеты по категориям и остатки:\n");
        w.getBudgets().forEach((cat, bud)-> {
            double spent = w.spentInCategory(cat);
            double remaining = bud - spent;
            sb.append(String.format("  %s: %,.2f, Оставшийся бюджет: %,.2f\n", cat, bud, remaining));
        });
        return sb.toString();
    }

    public String showTransactions() {
        if (!auth.isAuthenticated()) {
            return "Нужна авторизация";
        }
        Wallet w = auth.getCurrentUser().getWallet();
        StringBuilder sb = new StringBuilder();
        sb.append("Транзакции:\n");
        for(Transaction t: w.getTransactions()) {
            sb.append("  ").append(t).append("\n");
        }
        return sb.toString();
    }

    public String exportCsv() {
        if (!auth.isAuthenticated()) {
            return "Нужна авторизация";
        }
        Wallet w = auth.getCurrentUser().getWallet();
        StringBuilder sb = new StringBuilder();
        sb.append("type,category,amount,note,created\n");
        for(Transaction t: w.getTransactions()) {
            sb.append(t.getType()).append(",")
                    .append("\"").append(t.getCategory().replace("\"","'")).append("\",")
                    .append(t.getAmount()).append(",")
                    .append("\"").append(t.getNote().replace("\"","'")).append("\",")
                    .append(t.getCreatedStr())
                    .append("\n");
        }
        return sb.toString();
    }

    public String renameCategory(String oldCategory, String newCategory) {
        if (!auth.isAuthenticated()) {
            return "Нужна авторизация";
        }

        if (oldCategory == null || newCategory == null || oldCategory.trim().isEmpty() || newCategory.trim().isEmpty()) {
            return "Имена категорий не могут быть пустыми";
        }

        if (oldCategory.equals(newCategory)) {
            return "Новое имя категории совпадает со старым";
        }

        Wallet w = auth.getCurrentUser().getWallet();

        if (!w.hasCategory(oldCategory)) {
            return "Категория \"" + oldCategory + "\" не найдена";
        }

        if (w.hasCategory(newCategory)) {
            return "Категория \"" + newCategory + "\" уже существует";
        }

        if (w.renameCategory(oldCategory, newCategory)) {
            return String.format("Категория успешно переименована: \"%s\" → \"%s\"", oldCategory, newCategory);
        } else {
            return "Не удалось переименовать категорию";
        }
    }

    public String deleteCategory(String category, boolean force) {
        if (!auth.isAuthenticated()) {
            return "Нужна авторизация";
        }

        if (category == null || category.trim().isEmpty()) {
            return "Имя категории не может быть пустым";
        }

        Wallet w = auth.getCurrentUser().getWallet();

        if (!w.hasCategory(category)) {
            return "Категория \"" + category + "\" не найдена";
        }

        if (force) {
            // Принудительное удаление с транзакциями
            int removedTransactions = w.forceDeleteCategory(category);
            if (removedTransactions >= 0) {
                String message = String.format("Категория \"%s\" удалена. ", category);
                if (removedTransactions > 0) {
                    message += String.format("Также удалено %d транзакций.", removedTransactions);
                } else {
                    message += "Связанные транзакции отсутствовали.";
                }
                return message;
            } else {
                return "Не удалось удалить категорию";
            }
        } else {
            // Обычное удаление (без транзакций)
            if (w.deleteCategory(category)) {
                return String.format("Категория \"%s\" удалена", category);
            } else {
                // Проверяем, есть ли транзакции
                List<Transaction> transactions = w.getTransactionsByCategory(category);
                if (!transactions.isEmpty()) {
                    return String.format("Не удалось удалить категорию \"%s\". " + "С ней связаны %d транзакций. " + "Используйте force-delete-category для принудительного удаления.",
                                        category, transactions.size());
                } else {
                    return "Не удалось удалить категорию";
                }
            }
        }
    }

    public String showCategoryInfo(String category) {
        if (!auth.isAuthenticated()) {
            return "Нужна авторизация";
        }

        if (category == null || category.trim().isEmpty()) {
            return "Имя категории не может быть пустым";
        }

        Wallet w = auth.getCurrentUser().getWallet();

        if (!w.hasCategory(category)) {
            return "Категория \"" + category + "\" не найдена";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Информация о категории: \"%s\"\n", category));

        // Бюджет
        Double budget = w.getBudget(category);
        if (budget != null) {
            sb.append(String.format("  Бюджет: %.2f\n", budget));

            double spent = w.spentInCategory(category);
            sb.append(String.format("  Потрачено: %.2f\n", spent));

            double remaining = budget - spent;
            sb.append(String.format("  Остаток: %.2f (%.1f%%)\n", remaining, (spent / budget) * 100));
        } else {
            sb.append("  Бюджет: не установлен\n");
        }

        // Транзакции
        List<Transaction> transactions = w.getTransactionsByCategory(category);
        sb.append(String.format("  Количество транзакций: %d\n", transactions.size()));

        if (!transactions.isEmpty()) {
            sb.append("  Последние транзакции:\n");
            int count = Math.min(transactions.size(), 5);
            for (int i = 0; i < count; i++) {
                Transaction t = transactions.get(transactions.size() - 1 - i); // Сначала новые
                sb.append(String.format("    %s\n", t));
            }
        }

        return sb.toString();
    }

    public String listCategories() {
        if (!auth.isAuthenticated()) {
            return "Нужна авторизация";
        }

        Wallet w = auth.getCurrentUser().getWallet();
        Set<String> categories = w.getCategories();

        if (categories.isEmpty()) {
            return "Категории не созданы. Используйте create-category <name>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Категории:\n");

        for (String category : categories) {
            sb.append(String.format("  %s", category));

            Double budget = w.getBudget(category);
            if (budget != null) {
                double spent = w.spentInCategory(category);
                double remaining = budget - spent;
                sb.append(String.format(" | Бюджет: %.2f, Потрачено: %.2f, Остаток: %.2f", budget, spent, remaining));

                // Индикатор использования
                double percentage = (spent / budget) * 100;
                if (percentage >= 100) {
                    sb.append(" [ПРЕВЫШЕНИЕ]");
                } else if (percentage >= 80) {
                    sb.append(" [>80%]");
                }
            }

            int transactionCount = w.getTransactionsByCategory(category).size();
            sb.append(String.format(" | Транзакций: %d", transactionCount));

            sb.append("\n");
        }

        return sb.toString();
    }
}
