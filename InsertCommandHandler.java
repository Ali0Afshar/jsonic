package ir.ac.kntu;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
    }

    @Override
    public CommandResult handle(InputData inputData) throws IllegalArgumentException {
        if (!isValidInput(inputData.input))
            throw new IllegalArgumentException("Error: Invalid input.");

        Extractor.extractTypeName(inputData);

        Database db = Database.getInstance();
        if (db.getTypeFormatByType(inputData.typeName) == null) {
            throw new IllegalArgumentException("Error: This type not exists.");
        }

        Extractor.extractJson(inputData);

        checkJsonInput(inputData.json, db.getTypeFormatByType(inputData.typeName), 'i');

        checkJsonInputKey(inputData.json, db.getTypeFormatByType(inputData.typeName));

        checkJsonUnique(inputData.json, db.getAllDataByType(inputData.typeName), db.getTypeFormatByType(inputData.typeName));

        db.addData(inputData.typeName, inputData.json);
        return new CommandResult(PrintType.JSON, inputData.json);
    }

    private boolean isValidInput(String input) {
        Pattern pattern = Pattern.compile("^\\s*[a-zA-Z0-9_]+\\s+[a-zA-Z0-9_]+\\s*\\{.*\\}\\s*$");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find())
            return true;
        return false;
    }

    public static void checkJsonUnique(HashMap<String, Object> inputJson, ArrayList<HashMap<String, Object>> dataList, HashMap<String, Object> dataFormat) throws IllegalArgumentException {
        for (HashMap<String, Object> data : dataList)
            checkDataUnique(inputJson, data, dataFormat);
    }

    private static void checkDataUnique(HashMap<String, Object> inputJson, HashMap<String, Object> data, HashMap<String, Object> dataFormat) throws IllegalArgumentException {
        for (String key : inputJson.keySet()) {
            Object inputValue = inputJson.get(key);
            Object dataValue = data.get(key);

            if (inputValue instanceof HashMap) {
                checkDataUnique((HashMap<String, Object>) inputValue, (HashMap<String, Object>) dataValue, (HashMap<String, Object>) dataFormat.get(key));
                continue;
            }

            if ((Boolean) ((HashMap<String, Object>) dataFormat.get(key)).get("unique"))
                if (inputValue.equals(dataValue) && !DEFAULT_VALUE.values().contains(inputValue))
                    throw new IllegalArgumentException("Error: '" + inputValue + "' already exist. key '" + key + "' is unique.");
        }
    }

    public static void checkJsonInputKey(HashMap<String, Object> inputJson, HashMap<String, Object> jsonFormat) throws IllegalArgumentException {
        for (String key : inputJson.keySet()) {
            if (!jsonFormat.containsKey(key))
                throw new IllegalArgumentException("Error: Key '" + key + "' is not defined in the format.");
            if (inputJson.get(key) instanceof HashMap) {
                checkJsonInputKey((HashMap<String, Object>) inputJson.get(key), (HashMap<String, Object>) jsonFormat.get(key));
                continue;
            }
        }
    }

    public static void checkJsonInput(HashMap<String, Object> inputJson, HashMap<String, Object> jsonFormat, char className) throws IllegalArgumentException {
        for (String key : jsonFormat.keySet()) {
            HashMap<String, Object> valueFormat = (HashMap)jsonFormat.get(key);

            if (!inputJson.containsKey(key) && className == 'i')
                handleMissingKey(inputJson, valueFormat, key);

            Object valueInput = inputJson.get(key);
            if (isNestedJson(valueFormat)) {
                if (valueInput instanceof HashMap) {
                    checkJsonInput((HashMap<String, Object>) valueInput, valueFormat, className);
                    continue;
                }
                if (valueInput != null)
                    throw new IllegalArgumentException("Error: \'" + valueInput + "\' is wrong.");
            }

            if (valueInput instanceof HashMap)
                throw new IllegalArgumentException("Error: \'" + valueInput + "\' is wrong.");

            if (valueInput != null)
                checkValueType(valueInput, (String) valueFormat.get("type"), (String) valueFormat.get("items"));
        }        
    }

    private static void checkValueType(Object value, String type, String items) throws IllegalArgumentException {
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

    private static void checkListValueType(ArrayList<Object> value, String items) throws IllegalArgumentException {
        if (!value.isEmpty())
            checkValueType(value.get(0), items, null);
    }

    private static void handleMissingKey(HashMap<String, Object> inputJson, HashMap<String, Object> valueFormat, String key) throws IllegalArgumentException {
        if (isNestedJson(valueFormat)) {
            if (isPresentRequired(valueFormat))
                throw new IllegalArgumentException("Error: \'" + valueFormat + "\' is necessary");
        }
        else if ((Boolean) valueFormat.get("required"))
            throw new IllegalArgumentException("Error: \'" + key + ": " + valueFormat + "\' is necessary");
        else {
            if (valueFormat.get("type").equals("time"))
                inputJson.put(key, LocalDateTime.now());
            else
                inputJson.put(key, DEFAULT_VALUE.get(valueFormat.get("type")));
        }
    }

    private static boolean isPresentRequired(HashMap<String, Object> formatJson) {
        for (String key : formatJson.keySet()) {
            HashMap<String, Object> formatValue = (HashMap<String, Object>)formatJson.get(key);
            if (isNestedJson(formatValue)) {
                isPresentRequired(formatValue);
                continue;
            }
            
            if ((Boolean)formatValue.get("required"))
                return true;
        }

        return false;
    }

    private static boolean isNestedJson(HashMap<String, Object> json) {
        for (String key : json.keySet())
            if (json.get(key) instanceof HashMap)
                return true;

        return false;
    }
}
