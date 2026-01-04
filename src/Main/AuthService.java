import java.util.Map;

public class AuthService {
    private final Map<String, User> users;
    private User current;
    public AuthService(Map<String, User> users) {
        this.users = users;
    }
    public boolean register(String login, String password) {
        if(login == null || login.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }
        if(users.containsKey(login)) {
            return false;
        }
        User u = new User(login, password);
        users.put(login, u);
        return true;
    }
    public boolean login(String login, String password) {
        User u = users.get(login);
        if(u == null) {
            return false;
        }
        if(u.checkPassword(password)) {
            current = u;
            return true;
        }
        return false;
    }
    public void logout() {
        current = null; 
    }
    public boolean isAuthenticated() {
        return current != null; 
    }
    public User getCurrentUser() {
        return current; 
    }
    public Map<String, User> getUsersMap() {
        return users; 
    }
}
