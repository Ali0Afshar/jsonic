package ir.ac.kntu;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateCommandHandler implements CommandHandler {
    public void handle(InputData inputData) throws IllegalArgumentException {
        if (!isValidInput(inputData.input))
            throw new IllegalArgumentException("Error: Invalid input.");

        Extractor.extractTypeName(inputData);

        if (Database.getTypeFormatByType(inputData.typeName) != null) {
            throw new IllegalArgumentException("Error: This type already exists!");
        }
        
        Extractor.extractJson(inputData);

        if (inputData.json.isEmpty())
            throw new IllegalArgumentException("Error: JSON can't be empty.");

        checkJson(inputData.json);
    }

    private boolean isValidInput(String input) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]+\\s+[a-zA-Z0-9_]+\\s*\\{.*\\}$");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find())
            return true;
        return false;
    }

    private void checkJson(HashMap<String, Object> json) throws IllegalArgumentException {
        for (String key : json.keySet()) {
            if (!key.matches("[a-zA-Z0-9_]+"))
                throw new IllegalArgumentException("Error: invalid key \'" + key + "\' .");

            Object value = json.get(key);

            if (value instanceof HashMap) {
                if (((HashMap<String, Object>)value).isEmpty())
                    throw new IllegalArgumentException("Error: JSON can't be empty.");
                checkJson((HashMap<String, Object>) value);
            }
            else {
                if (!json.containsKey("type"))
                    throw new IllegalArgumentException("Error: Key type should be appear in " + json + ".");

                if (key.equals("type")) {
                    if (!isValidType((String) value))
                        throw new IllegalArgumentException("Error: Type " + value + " is not valid.");
                }
                else if (key.equals("items")) {
                    if (json.get("type").equals("list")) {
                        if (value.equals("list"))
                            throw new IllegalArgumentException("Error: Nested list is not allowed.");
                        if (!isValidType((String) value))
                            throw new IllegalArgumentException("Error: Type " + value + " is not valid.");
                    }
                    else
                        throw new IllegalArgumentException("Error: Key items is not allowed for type " + json.get("type") + ".");
                }
                else if (key.equals("required")) {
                    if (!(value instanceof Boolean))
                        throw new IllegalArgumentException("Error: Required should be true/false, not " + value + " .");
                }
                else if (key.equals("unique")) {
                    if (!(value instanceof Boolean))
                        throw new IllegalArgumentException("Error: Unique should be true/false not " + value + " .");
                }
                else
                    throw new IllegalArgumentException("Error: Key " + key + " invalid. Valid keys are type, required and unique.");
            }
        }
    }

    private boolean isValidType(String type) {
        return type.equals("string") || type.equals("int") || type.equals("bool") || 
               type.equals("list") || type.equals("dbl") || type.equals("time");
    }
}
