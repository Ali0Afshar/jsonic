package ir.ac.kntu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        InputData inputData = new InputData();
        Scanner scanner = new Scanner(System.in);

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

class Condition {
    String field;
    String operator;
    String value;

    public Condition(String field, String operator, String value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public String toString() {
        return field + " " + operator + " " + value;
    }
}
