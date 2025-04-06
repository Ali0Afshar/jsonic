package ir.ac.kntu;

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
    }

    private boolean isValidInput(String input) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]+\\s+[a-zA-Z0-9_]+\\s*\\{.*\\}$");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find())
            return true;
        return false;
    }
}
