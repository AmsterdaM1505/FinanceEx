import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class StorageService {
    private final File file;

    public StorageService(String filename){
        this.file = new File(filename);
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();  // создает все необходимые директории
        }
    }

    public Map<String, User> loadAll(){
        if (!file.exists()) {
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){
            Object o = ois.readObject();
            if (o instanceof Map) {
                return (Map<String, User>) o;
            }
            return new HashMap<>();
        } catch (Exception e){
            System.err.println("Не удалось загрузить данные: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public void saveAll(Map<String, User> map){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))){
            oos.writeObject(map);
        } catch (IOException e){
            System.err.println("Ошибка при сохранении: " + e.getMessage());
        }
    }
}
