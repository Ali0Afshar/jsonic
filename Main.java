package ir.ac.kntu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        do {
            InputData inputData = new InputData();
            inputData.input = scanner.nextLine();
            inputData.input = inputData.input.toLowerCase();

            if (inputData.input.equals("exit")) {
                scanner.close();
                System.exit(0);
            }

            try {
                Extractor.extractCommand(inputData);
                CommandProcessor processor = new CommandProcessor();
                processor.processCommand(inputData);
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } while (true);
    }
}

class InputData {
    String input;
    String command;
    String typeName;
    ArrayList<Condition> conditions;
    HashMap<String, Object> json;
    String errorMessage;
}

enum OperandType {
    FIELD, NUMBER, STRING, BOOLEAN, DATETIME, LIST
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

    public String toString() {
        return operand1 + " " + operator + " " + operand2;
    }
}
