import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Type {
        INCOME, 
        EXPENSE 
    }

    private final Type type;
    private final String category;
    private final double amount;
    private final String note;
    private final LocalDateTime created;

    public Transaction(Type type, String category, double amount, String note) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.note = note == null ? "" : note;
        this.created = LocalDateTime.now();
    }

    public Type getType() {
        return type;
    }
    public String getCategory() {
        return category; 
    }
    public double getAmount() {
        return amount; 
    }
    public String getNote() {
        return note; 
    }
    public String getCreatedStr() {
        return created.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")); 
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s: %.2f %s", getCreatedStr(), type, category, amount, note);
    }
}
