package ir.ac.kntu;

import java.util.HashMap;
import java.util.ArrayList;

public class Database {
    public HashMap<String, ArrayList<HashMap<String, Object>>> data = new HashMap<>();
    public HashMap<String, HashMap<String, Object>> typeFormat = new HashMap<>();

    private static final Database instance = new Database();

    private Database() {
        data = new HashMap<>();
        typeFormat = new HashMap<>();
    }

    public static Database getInstance() {
        return instance;
    }

    public HashMap<String, Object> getTypeFormatByType(String key) {
        return typeFormat.get(key);
    }

    public void addNewTypeFormat(String key, HashMap<String, Object> value) {
        typeFormat.put(key, value);
    }
}
