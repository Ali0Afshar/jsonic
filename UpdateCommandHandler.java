package ir.ac.kntu;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateCommandHandler implements CommandHandler{
    public void handle(InputData inputData) {
        if (!isValidInput(inputData.input))
            throw new IllegalArgumentException("Error: Invalid input.");

        Extractor.extractTypeName(inputData);

        if (!Extractor.extractConditions(inputData))
            inputData.conditions = null;

        if (!Extractor.extractJson(inputData))
            throw new IllegalArgumentException("Error: JSON doesn't exist");

        
    }

    private boolean isValidInput(String input) {
        Pattern pattern = Pattern.compile("^\\s*[a-zA-Z0-9_]+\\s+[a-zA-Z0-9_]+(?:\\s+\\([^\\s()][^()]*[^\\s()]\\))?\\s+\\{.*");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find())
            return true;
        return false;
    }
}
