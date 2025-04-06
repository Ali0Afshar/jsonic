package ir.ac.kntu;

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
}
