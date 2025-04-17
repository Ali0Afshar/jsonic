package ir.ac.kntu;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        InputData inputData = new InputData();

        do {
            inputData.input = scanner.nextLine();
            inputData.input = inputData.input.toLowerCase();

            if (inputData.input.equals("exit")) {
                scanner.close();
                System.exit(0);
            }

            try {
                Extractor.extractCommand(inputData);
                CommandProcessor processor = new CommandProcessor();
                CommandResult result = processor.processCommand(inputData);
                customPrint(result);
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } while (true);
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static void customPrint(CommandResult result) {
        switch (result.getType()) {
            case PrintType.TEXT:
                System.out.println(result.getValue());
                break;
            case PrintType.JSON:
                printJson((HashMap<String, Object>)result.getValue(), 0);
                System.out.println();
                break;
            case PrintType.LIST:
                for (HashMap<String, Object> json : (ArrayList<HashMap<String, Object>>)result.getValue()) {
                    printJson(json, 0);
                    System.out.println();
                    System.out.println("-------------------------------------------------------------");
                }
                break;
            default:
                break;
        }
    }

    private static void printJson(Object obj, int indent) {
        String indentStr = "  ".repeat(indent);
    
        if (obj instanceof HashMap) {
            System.out.println(indentStr + "{");
            HashMap<String, Object> map = (HashMap<String, Object>) obj;
            int count = 0;
    
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                System.out.print("  ".repeat(indent + 1));
                System.out.print(Color.CYAN + "\"" + entry.getKey() + "\"" + Color.RESET + ": ");
                printJson(entry.getValue(), indent + 1);
                count++;
                if (count < map.size())
                    System.out.print(",");
                System.out.println();
            }
    
            System.out.print(indentStr + "}");
        }
        else if (obj instanceof List) {
            List list = (List) obj;
            System.out.print("[");
    
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                printJson(item, 0);
                if (i < list.size() - 1)
                    System.out.print(", ");
            }
    
            System.out.print("]");
        }
        else if (obj instanceof String)
            System.out.print(Color.YELLOW + "\"" + obj + "\"" + Color.RESET);
        else if (obj instanceof Number)
            System.out.print(Color.PURPLE + "" + obj + Color.RESET);
        else if (obj instanceof Boolean)
            System.out.print(Color.BLUE + "" + obj + Color.RESET);
        else if (obj instanceof LocalDateTime)
            System.out.print(Color.RED + "\"" + ((LocalDateTime) obj).format(DATE_TIME_FORMATTER) + "\"" + Color.RESET);
    }
}

class InputData {
    String input;
    String command;
    String typeName;
    ArrayList<Condition> conditions;
    HashMap<String, Object> json;
}

enum OperandType {
    FIELD, NUMBER, STRING, BOOLEAN, DATETIME, LIST
}

enum PrintType {
    TEXT, JSON, LIST
}

enum Color {
    RESET("\u001B[0m"),
    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    PURPLE("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[37m");

    private final String code;

    Color(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}

class Operand {
    OperandType type;
    Object value;
    OperandType fieldType;
    OperandType listType;

    public Operand() {}

    public Operand(OperandType type, Object value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "" + value + "";
    }
}

class Condition {
    Operand operand1;
    String operator;
    Operand operand2;

    public Condition(Operand operand1, String operator, Operand operand2) {
        this.operand1 = operand1;
        this.operator = operator;
        this.operand2 = operand2;
    }

    @Override
    public String toString() {
        return operand1 + " " + operator + " " + operand2;
    }
}
