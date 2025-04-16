package ir.ac.kntu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateCommandHandler implements CommandHandler{
    @Override
    public CommandResult handle(InputData inputData) {
        if (!isValidInput(inputData.input))
            throw new IllegalArgumentException("Error: Invalid input.");

        Extractor.extractTypeName(inputData);
        checkTypeExist(inputData.typeName);

        if (Extractor.isConditionPresent(inputData.input))
            Extractor.extractConditions(inputData);
        else
            inputData.conditions = null;

        Extractor.extractJson(inputData);

        Database db = Database.getInstance();
        HashMap<String, Object> typeFormat = db.getTypeFormatByType(inputData.typeName);
        InsertCommandHandler.checkJsonInput(inputData.json, typeFormat, 'u');
        InsertCommandHandler.checkJsonInputKey(inputData.json, typeFormat);
        InsertCommandHandler.checkJsonUnique(inputData.json, db.getAllDataByType(inputData.typeName), typeFormat);
        ArrayList<Integer> indexes = db.getIndexFilteredData(inputData.typeName, inputData.conditions);
        if (indexes.size() > 1)
            checkUnique(typeFormat, inputData.json);

        int result = db.updateDataByIndex(inputData.typeName, indexes, inputData.json);
        return new CommandResult(PrintType.TEXT, Color.GREEN + "Update completed successfully. '" + result + "' samples updated." + Color.RESET);
    }

    private boolean isValidInput(String input) {
        Pattern pattern = Pattern.compile("^\\s*[a-zA-Z0-9_]+\\s+[a-zA-Z0-9_]+(?:\\s+\\([^\\s()][^()]*[^\\s()]\\))?\\s+\\{.*\\s*\\}\\s*$");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find())
            return true;
        return false;
    }

    private void checkTypeExist(String type) throws IllegalArgumentException {
        Database db = Database.getInstance();
        if (db.getTypeFormatByType(type) == null)
            throw new IllegalArgumentException("Error: Type '" + type + "' not exist.");
    }

    private void checkUnique(HashMap<String, Object> format, HashMap<String, Object> inputJson) throws IllegalArgumentException {
        for (String key : inputJson.keySet()) {
            Object inputValue = inputJson.get(key);
            if (inputValue instanceof HashMap) {
                checkUnique((HashMap)format.get(key), (HashMap)inputValue);
                continue;
            }

            if ((Boolean)((HashMap)format.get(key)).get("unique"))
                throw new IllegalArgumentException("Error: key '" + key + "' is unique.");
        }
    }
}
