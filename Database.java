package ir.ac.kntu;

import java.util.HashMap;
import java.util.ArrayList;

public class Database {
    public HashMap<String, ArrayList<HashMap<String, Object>>> data;
    public HashMap<String, HashMap<String, Object>> typeFormat;

    private static final Database instance = new Database();

    private Database() {
        data = new HashMap<>();
        typeFormat = new HashMap<>();
    }

    public static Database getInstance() {
        return instance;
    }

    public HashMap<String, Object> getTypeFormatByType(String type) {
        return typeFormat.get(type);
    }

    public void addNewTypeFormat(String key, HashMap<String, Object> value) {
        typeFormat.put(key, value);
    }

    public ArrayList<HashMap<String, Object>> getAllDataByType(String type) {
        ArrayList<HashMap<String, Object>> result = data.get(type);
        if (result == null)
            result = new ArrayList<>();
        
        return result;
    }

    public void addData(String type, HashMap<String, Object> newData) {
        ArrayList<HashMap<String, Object>> dataList = data.get(type);
        if (dataList == null) {
            dataList = new ArrayList<>();
            data.put(type, dataList);
        }

        dataList.add(newData);
        
    }
}
