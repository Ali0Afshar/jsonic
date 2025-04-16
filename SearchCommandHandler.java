package ir.ac.kntu;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchCommandHandler implements CommandHandler {
    @Override
    public void handle(InputData inputData) throws IllegalArgumentException {
        if (!isValidInput(inputData.input))
            throw new IllegalArgumentException("Error: Invalid input.");

        Extractor.extractTypeName(inputData);
        checkTypename(inputData.typeName);
        
        if (Extractor.isConditionPresent(inputData.input))
            Extractor.extractConditions(inputData);
        else
            inputData.conditions = null;
            
        Database db = Database.getInstance();
        for (HashMap<String, Object> data : db.getFilteredData(inputData.typeName, inputData.conditions)) {
            System.out.println(data);
        }
        // No data matches these conditions.
    }

    private boolean isValidInput(String input) {
        Pattern pattern = Pattern.compile("^\\s*[A-Za-z0-9_]+\\s+[A-Za-z0-9_]+(\\s*\\(.*\\))?\\s*$");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find())
            return true;
        return false;
    }

    private void checkTypename(String type) throws IllegalArgumentException {
        Database db = Database.getInstance();
        if (db.getTypeFormatByType(type) == null)
            throw new IllegalArgumentException("Error: Type '" + type + "' not exist.");
    }
}
