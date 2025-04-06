package ir.ac.kntu;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Extractor {
    public static void extractCommand(InputData inputData) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("^\\s*([a-zA-Z0-9_]+)");
        Matcher matcher = pattern.matcher(inputData.input);
        if (matcher.find())
            inputData.command = matcher.group(1).trim();
        else
            throw new IllegalArgumentException("Error: Invalid command format");
    }

    public static void extractTypeName(InputData inputData) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("^\\s*[a-zA-Z0-9_]+\\s+([a-zA-Z0-9_]+)");
        Matcher matcher = pattern.matcher(inputData.input);

        if (matcher.find())
            inputData.typeName = matcher.group(1).trim();
        else
            throw new IllegalArgumentException("Error: Invalid type format");
    }

    public static void extractJson(InputData inputData) throws IllegalArgumentException {
        String input = inputData.input.trim();
        int start = input.indexOf('{');
        if (start == -1)
            throw new IllegalArgumentException("Error: JSON doesn't exist");

        int count = 0, end = -1;
        for (int i = start; i < input.length(); i++) {
            if (input.charAt(i) == '{')
                count++;
            if (input.charAt(i) == '}')
                count--;

            if (count < 0)
                throw new IllegalArgumentException("Error: Invalid JSON format");
            if (count == 0) {
                end = i;
                break;
            }
        }

        if (count != 0)
            throw new IllegalArgumentException("Error: Invalid JSON format");

        String jsonInput = input.substring(start, end + 1).trim();
        inputData.json = parseJSON(jsonInput);
    }

    private static HashMap<String, Object> parseJSON(String json) throws IllegalArgumentException {
        HashMap<String, Object> hashMap = new HashMap<>();
        json = json.substring(1, json.length() - 1).trim();

        ArrayList<String> pairs = extractKeyValuePairs(json);
        
        for (String pair : pairs) {
            pair = pair.trim();

            String[] keyValue = pair.split(":", 2);
            if (keyValue.length != 2)
                throw new IllegalArgumentException(
                    "Error: Invalid key-value pair {" + pair + "}. Each entry must be in the format \"key\": value.");

            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            if (!(key.startsWith("\"") && key.endsWith("\"")))
                throw new IllegalArgumentException("Error: Key " + key + " must be enclosed in double quotes.");
            key = key.substring(1, key.length() - 1);

            Object parsedValue = parseValue(value);
            if (hashMap.get(key) != null)
                throw new IllegalArgumentException(
                    "Error: Can't use one key (" + key + ") for two value (" + hashMap.get(key) + ", " + parsedValue + ").");
            hashMap.put(key, parsedValue);
        }

        return hashMap;
    }

    private static ArrayList<String> extractKeyValuePairs(String json) throws IllegalArgumentException {
        ArrayList<String> pairs = new ArrayList<>();
        StringBuilder currentPair = new StringBuilder();
        int braceCount = 0, bracketCount = 0;
        boolean insideString = false;
    
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
    
            if (c == '"')
                insideString = !insideString;
            else if (!insideString) {
                if (c == '{') 
                    braceCount++;
                else if (c == '}') 
                    braceCount--;
                else if (c == '[') 
                    bracketCount++;
                else if (c == ']') 
                    bracketCount--;
            }

            if (braceCount < 0 || bracketCount < 0)
                throw new IllegalArgumentException("Error: Invalid JSON format");
    
            if (c == ',' && braceCount == 0 && bracketCount == 0 && !insideString) {
                pairs.add(currentPair.toString().trim());
                currentPair.setLength(0);
            }
            else {
                currentPair.append(c);
            }
        }
    
        if (currentPair.toString().trim().length() > 0) {
            pairs.add(currentPair.toString().trim());
        }
    
        return pairs;
    }
    
    private static Object parseValue(String value) throws IllegalArgumentException {
        value = value.trim();

        if (value.equals("true"))
            return true;
        if (value.equals("false"))
            return false;
        if (value.equals("null"))
            return null;
        if (value.startsWith("\"") && value.endsWith("\""))
            return value.substring(1, value.length() - 1);
        if (value.startsWith("{") && value.endsWith("}"))
            return parseJSON(value);
        if (value.startsWith("[") && value.endsWith("]"))
            return parseArray(value);
        try {
            if (value.contains("."))
                return Double.parseDouble(value);
            else
                return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("Error: Invalid value " + value + ".");
            }
        }
    }

    private static ArrayList<Object> parseArray(String stringArray) throws IllegalArgumentException {
        stringArray = stringArray.substring(1, stringArray.length() - 1).trim();
        if (stringArray.isEmpty()) {
            throw new IllegalArgumentException("List cannot be empty.");
        }
    
        ArrayList<Object> arrayList = new ArrayList<>();
        String[] elements = stringArray.split(",");
    
        Object firstType = null;
        for (String element : elements) {
            Object parsedValue = parseValue(element.trim());
            
            if (firstType == null && parsedValue != null)
                firstType = parsedValue.getClass();
    
            if (parsedValue != null && firstType != parsedValue.getClass()) {
                throw new IllegalArgumentException("JSON array must contain elements of the same type");
            }
    
            arrayList.add(parsedValue);
        }
    
        return arrayList;
    }
}
    