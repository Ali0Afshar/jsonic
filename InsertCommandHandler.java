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
    }

    private boolean isValidInput(String input) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]+\\s+[a-zA-Z0-9_]+\\s*\\{.*\\}$");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find())
            return true;
        return false;
    }

}
