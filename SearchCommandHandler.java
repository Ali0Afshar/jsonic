package ir.ac.kntu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchCommandHandler implements CommandHandler {
    @Override
    public CommandResult handle(InputData inputData) throws IllegalArgumentException {
        if (!isValidInput(inputData.input))
            throw new IllegalArgumentException("Error: Invalid input.");

        Extractor.extractTypeName(inputData);
        checkTypename(inputData.typeName);
        
        if (Extractor.isConditionPresent(inputData.input))
            Extractor.extractConditions(inputData);
        else
            inputData.conditions = null;
            
        Database db = Database.getInstance();
        ArrayList<HashMap<String, Object>> result = db.getFilteredData(inputData.typeName, inputData.conditions);
        
        if (result == null) 
            return new CommandResult(PrintType.TEXT, Color.YELLOW + "No result found." + Color.RESET);
        else 
            return new CommandResult(PrintType.LIST, result);
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
