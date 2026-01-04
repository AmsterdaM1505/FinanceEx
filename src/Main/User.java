import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String login;
    private final String passwordHash;
    private Wallet wallet;

    public User(String login, String password) {
        this.login = login;
        this.passwordHash = Integer.toString(Objects.requireNonNull(password).hashCode());
        this.wallet = new Wallet();
    }

    public String getLogin() {
        return login;
    }
    public boolean checkPassword(String password) {
        return this.passwordHash.equals(Integer.toString(password.hashCode()));
    }
    public Wallet getWallet() {
        return wallet;
    }
    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
}
