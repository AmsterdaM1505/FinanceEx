import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Scanner;

// во всех файлах решения используется try-catch для четкости ошибок
// для четкости ошибок, в том числе предусмотрены обычные комментарии через System.out.println

public class Main {
    private static final String STORAGE_FILE = "data/data.ser"; // файл с данными

    public static void main(String[] args) {
        StorageService storage = new StorageService(STORAGE_FILE);
        Map<String, User> users = storage.loadAll();
        AuthService auth = new AuthService(users);
        FinanceService finance = new FinanceService(auth);

        Scanner sc = new Scanner(System.in);
        System.out.println("Приложение: Личные финансы");
        System.out.println("Для справки по командам используйте help");

        boolean running = true;
        while(running){
            System.out.print("> ");
            String line = sc.nextLine(); // чтение команд
            if (line == null) break;
            String[] tokens = line.trim().split("\\s+", 2); // вытаскиваем команду
            if (tokens.length==0 || tokens[0].isEmpty()) continue;
            String cmd = tokens[0].toLowerCase();
            String arg = tokens.length>1?tokens[1]:"";

            try {
                switch(cmd){
                    case "help": printHelp(); break;
                    case "register": { // регистрация
                        String[] a = arg.split("\\s+");
                        if (a.length<2) {  // проверка вводимых команд
                            System.out.println("Использование: register <login> <password>"); // таким образом подсказываем, как написать если возникла ошибка в команде
                        } else {
                            if (auth.register(a[0], a[1])){ // проверка вводимых данных
                                storage.saveAll(users);
                                System.out.println("Пользователь зарегистрирован.");
                            } else System.out.println("Ошибка регистрации (возможно пользователь уже существует или неверные данные)."); // уведомление о некорректных данных
                        }
                        break;
                    }
                    case "login": { // авторизация по логину и паролю, несколько пользователей
                        String[] a = arg.split("\\s+");
                        if (a.length<2) System.out.println("Использование: login <login> <password>");
                        else {
                            if (auth.login(a[0], a[1])){
                                System.out.println("Успешно: вошли как " + a[0]);
                            } else System.out.println("Ошибка: неверный логин/пароль.");
                        }
                        break;
                    }
                    case "logout": {
                        auth.logout();
                        System.out.println("Выход.");
                        break;
                    }
                    case "create-category": { // создание категории для планирования бюджета
                        System.out.println(finance.createCategory(arg));
                        storage.saveAll(users);
                        break;
                    }
                    case "set-budget": { // установление бюджета на категорию
                        String[] a = arg.split("\\s+");
                        if (a.length<2) System.out.println("Использование: set-budget <category> <amount>");
                        else {
                            try {
                                double amt = Double.parseDouble(a[1]);
                                System.out.println(finance.setBudget(a[0], amt));
                                storage.saveAll(users);
                            } catch(NumberFormatException e){ System.out.println("Неверный формат суммы."); }
                        }
                        break;
                    }
                    case "add-income": { // добавление дохода
                        String[] a = arg.split("\\s+",3);
                        if (a.length<2) System.out.println("Использование: add-income <category> <amount> ");
                        else {
                            try {
                                double amt = Double.parseDouble(a[1]);
                                System.out.println(finance.addIncome(a[0], amt, a.length==3? a[2] : ""));
                                storage.saveAll(users);
                            } catch(NumberFormatException e){ System.out.println("Неверный формат суммы."); }
                        }
                        break;
                    }
                    case "add-expense": { // добавление расхода
                        String[] a = arg.split("\\s+",3);
                        if (a.length<2) System.out.println("Использование: add-expense <category> <amount> ");
                        else {
                            try {
                                double amt = Double.parseDouble(a[1]);
                                System.out.println(finance.addExpense(a[0], amt, a.length==3? a[2] : ""));
                                storage.saveAll(users);
                            } catch(NumberFormatException e){ System.out.println("Неверный формат суммы."); }
                        }
                        break;
                    }
                    case "transfer": {
                        String[] a = arg.split("\\s+",3);
                        if (a.length<2) System.out.println("Использование: transfer <toLogin> <amount> ");
                        else {
                            try {
                                double amt = Double.parseDouble(a[1]);
                                System.out.println(finance.transfer(a[0], amt, a.length==3? a[2] : ""));
                                storage.saveAll(users);
                            } catch(NumberFormatException e){ System.out.println("Неверный формат суммы."); }
                        }
                        break;
                    }
                    case "summary": { // вывод информации - общая сумма доходов и расходов и данные по каждой категории
                        System.out.println(finance.summary());
                        break;
                    }
                    case "transactions": {
                        System.out.println(finance.showTransactions());
                        break;
                    }
                    case "export-csv": {
                        String content = finance.exportCsv();
                        try(PrintWriter pw = new PrintWriter(new FileWriter("data/export_"+(auth.isAuthenticated()?auth.getCurrentUser().getLogin():"anon")+".csv"))){
                            pw.print(content);
                        }
                        System.out.println("Экспортирован CSV в файл.");
                        break;
                    }
                    case "save": { // просто сохранение по требованию
                        storage.saveAll(users);
                        System.out.println("Данные сохранены.");
                        break;
                    }
                    case "exit": { // окончание работы, выход из постоянного цикла
                        storage.saveAll(users); // сохранение данных в файл перед выходом
                        running = false;
                        System.out.println("Сохранено. Выход.");
                        break;
                    }
                    case "rename-category": {
                        String[] a = arg.split("\\s+", 2);
                        if (a.length < 2) {
                            System.out.println("Использование: rename-category <старое_имя> <новое_имя>");
                        } else {
                            System.out.println(finance.renameCategory(a[0], a[1]));
                            storage.saveAll(users);
                        }
                        break;
                    }
                    case "delete-category": {
                        if (arg.trim().isEmpty()) {
                            System.out.println("Использование: delete-category <имя_категории>");
                        } else {
                            System.out.println(finance.deleteCategory(arg, false));
                            storage.saveAll(users);
                        }
                        break;
                    }
                    case "force-delete-category": {
                        if (arg.trim().isEmpty()) {
                            System.out.println("Использование: force-delete-category <имя_категории>");
                        } else {
                            System.out.println(finance.deleteCategory(arg, true));
                            storage.saveAll(users);
                        }
                        break;
                    }
                    case "category-info": {
                        if (arg.trim().isEmpty()) {
                            System.out.println("Использование: category-info <имя_категории>");
                        } else {
                            System.out.println(finance.showCategoryInfo(arg));
                        }
                        break;
                    }
                    case "list-categories": {
                        System.out.println(finance.listCategories());
                        break;
                    }
                    default:
                        System.out.println("Неизвестная команда. help для списка команд.");
                }
            } catch(Exception ex){
                System.out.println("Ошибка: " + ex.getMessage());
            }
        }

        sc.close();
    }

    private static void printHelp(){
        System.out.println("Команды:");
        System.out.println("1. register <login> <password>");
        System.out.println("2. login <login> <password>");
        System.out.println("3. logout");
        System.out.println("4. create-category <name> | создание категории");
        System.out.println("5. rename-category <old> <new> | переименование категории");
        System.out.println("6. delete-category <name> | удаление категории (без транзакций)");
        System.out.println("7. force-delete-category <name> | принудительное удаление с транзакциями");
        System.out.println("8. category-info <name> | информация о категории");
        System.out.println("9. list-categories | список всех категорий");
        System.out.println("10. set-budget <category> <amount> | установка бюджета");
        System.out.println("11. add-income <category> <amount> | добавление дохода");
        System.out.println("12. add-expense <category> <amount> | добавление расхода");
        System.out.println("13. transfer <toLogin> <amount> | перевод другому пользователю");
        System.out.println("14. transactions | просмотр транзакций");
        System.out.println("15. summary | сводка");
        System.out.println("16. export-csv | экспорт в CSV");
        System.out.println("17. save | сохранение данных");
        System.out.println("18. exit | выход");
    }
}
