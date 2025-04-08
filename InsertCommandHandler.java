package ir.ac.kntu;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InsertCommandHandler implements CommandHandler {
    private static final HashMap<String, Object> DEFAULT_VALUE = new HashMap<>();

    static {
        DEFAULT_VALUE.put("string", "");
        DEFAULT_VALUE.put("int", 0);
        DEFAULT_VALUE.put("dbl", 0.0);
        DEFAULT_VALUE.put("bool", false);
        DEFAULT_VALUE.put("list", new ArrayList<>());
        DEFAULT_VALUE.put("time", LocalDateTime.now());
    }

    public void handle(InputData inputData) throws IllegalArgumentException {
        if (!isValidInput(inputData.input))
            throw new IllegalArgumentException("Error: Invalid input.");

        Extractor.extractTypeName(inputData);

        Database db = Database.getInstance();
        if (db.getTypeFormatByType(inputData.typeName) == null) {
            throw new IllegalArgumentException("Error: This type not exists.");
        }

        Extractor.extractJson(inputData);

        checkJsonInput(inputData.json, db.getTypeFormatByType(inputData.typeName));

    }

    private boolean isValidInput(String input) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]+\\s+[a-zA-Z0-9_]+\\s*\\{.*\\}$");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find())
            return true;
        return false;
    }

    private void checkJsonInput(HashMap<String, Object> inputJson, HashMap<String, Object> jsonFormat) throws IllegalArgumentException {
        for (String key : jsonFormat.keySet()) {
            HashMap<String, Object> valueFormat = (HashMap)jsonFormat.get(key);

            if (!inputJson.containsKey(key))
                handleMissingKey(inputJson, valueFormat, key);

            Object valueInput = inputJson.get(key);
            if (isNestedJson(valueFormat)) {
                if (valueInput instanceof HashMap) {
                    checkJsonInput(inputJson, jsonFormat);
                    return;
                }
                throw new IllegalArgumentException("Error: \'" + valueInput + "\' is wrong.");
            }

            if (valueInput instanceof HashMap)
                throw new IllegalArgumentException("Error: \'" + valueInput + "\' is wrong.");

            checkValueType(valueInput, (String) valueFormat.get("type"), (String) valueFormat.get("items"));
        }        
    }

    private void checkValueType(Object value, String type, String items) throws IllegalArgumentException {
        switch (type) {
            case "string":
                if (!(value instanceof String))
                    throw new IllegalArgumentException("Error: \'" + value + "\' is not of type \'string\'.");
                break;
            case "int":
                if (!(value instanceof Integer))
                    throw new IllegalArgumentException("Error: \'" + value + "\' is not of type \'int\'.");
                break;
            case "dbl":
                if (!(value instanceof Double))
                    throw new IllegalArgumentException("Error: \'" + value + "\' is not of type \'dbl\'.");
                break;
            case "bool":
                if (!(value instanceof Boolean))
                    throw new IllegalArgumentException("Error: \'" + value + "\' is not of type \'bool\'.");
                break;
            case "time":
                if (!(value instanceof LocalDateTime))
                    throw new IllegalArgumentException("Error: \'" + value + "\' is not of type \'time\'.");
                break;
            case "list":
                if (!(value instanceof ArrayList))
                    throw new IllegalArgumentException("Error: \'" + value + "\' is not of type \'list\'.");
                checkListValueType((ArrayList) value, items);
                break;
        }
    }

    private void checkListValueType(ArrayList<Object> value, String items) throws IllegalArgumentException {
        if (value.isEmpty())
            throw new IllegalArgumentException("Error: List can't be empty.");

        checkValueType(value.get(0), items, null);
    }

    private void handleMissingKey(HashMap<String, Object> inputJson, HashMap<String, Object> valueFormat, String key) throws IllegalArgumentException {
        if (isNestedJson(valueFormat))
            throw new IllegalArgumentException("Error: \'" + valueFormat + "\' is necessary");
        else if ((Boolean) valueFormat.get("required"))
            throw new IllegalArgumentException("Error: \'" + key + ": " + valueFormat + "\' is necessary");
        else 
            inputJson.put(key, DEFAULT_VALUE.get(valueFormat.get("type")));
    }

    private boolean isNestedJson(HashMap<String, Object> json) {
        for (String key : json.keySet())
            if (json.get(key) instanceof HashMap)
                return true;

        return false;
    }
}
