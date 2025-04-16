package ir.ac.kntu;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Database {
    public HashMap<String, ArrayList<HashMap<String, Object>>> data;
    public HashMap<String, HashMap<String, Object>> typeFormat;

    private static final Database instance = new Database();

    private Database() {
        data = new HashMap<>();
        typeFormat = new HashMap<>();
    }

    public static Database getInstance() {
        return instance;
    }

    public HashMap<String, Object> getTypeFormatByType(String type) {
        return typeFormat.get(type);
    }

    public void addNewTypeFormat(String key, HashMap<String, Object> value) {
        typeFormat.put(key, value);
    }

    public ArrayList<HashMap<String, Object>> getAllDataByType(String type) {
        ArrayList<HashMap<String, Object>> result = data.get(type);
        if (result == null)
            result = new ArrayList<>();
        
        return result;
    }

    public void addData(String type, HashMap<String, Object> newData) {
        ArrayList<HashMap<String, Object>> dataList = data.get(type);
        if (dataList == null) {
            dataList = new ArrayList<>();
            data.put(type, dataList);
        }

        dataList.add(newData);
        
    }

    public ArrayList<HashMap<String, Object>> getFilteredData(String type, ArrayList<Condition> conditions) throws IllegalArgumentException {
        if (conditions == null)
            return getAllDataByType(type);
        
        ArrayList<HashMap<String, Object>> filteredData = new ArrayList<>();
        for (HashMap<String,Object> sampleData : getAllDataByType(type))
            if (isValidSampleData(sampleData, conditions))
                filteredData.add(sampleData);

        return filteredData;
    }

    public ArrayList<Integer> getIndexFilteredData(String type, ArrayList<Condition> conditions) throws IllegalArgumentException {
        ArrayList<Integer> indexes = new ArrayList<>();
        int i = 0;

        for (HashMap<String,Object> sampleData : getAllDataByType(type)) {
            if (conditions == null || isValidSampleData(sampleData, conditions))
                indexes.add(i);
            i++;
        }

        return indexes;
    }

    public int updateDataByCondition(String type, ArrayList<Condition> conditions, HashMap<String,Object> newData) throws IllegalArgumentException {
        int counter = 0;

        for (HashMap<String,Object> lastData : getAllDataByType(type))
            if (conditions == null || isValidSampleData(lastData, conditions)) {
                updateSampleData(type, newData, lastData);
                counter++;
            }

        return counter;
    }

    public int updateDataByIndex(String type, ArrayList<Integer> indexes, HashMap<String,Object> newData) {
        int counter = 0;

        for (int index : indexes) {
            updateSampleData(type, newData, getAllDataByType(type).get(index));
            counter++;
        }

        return counter;
    }

    private void updateSampleData(String type, HashMap<String,Object> newData, HashMap<String,Object> lastData) {
        for (String key : newData.keySet()) {
            lastData.put(key, newData.get(key));
        }
    }

    private boolean isValidSampleData(HashMap<String,Object> sampleData, ArrayList<Condition> conditions) {
        for (Condition condition : conditions) {
            Operand operand1 = condition.operand1;
            Operand operand2 = condition.operand2;
            Object sampleDataValue1;
            Object sampleDataValue2;

            if (operand1.type == OperandType.FIELD && operand2.type == OperandType.FIELD) {
                sampleDataValue1 = extractSampleDataValue(operand1, sampleData);
                sampleDataValue2 = extractSampleDataValue(operand2, sampleData);
                return isConditionTrue(sampleDataValue1, condition.operator, sampleDataValue2);
            }
            else if (operand1.type == OperandType.FIELD) {
                sampleDataValue1 = extractSampleDataValue(operand1, sampleData);
                return isConditionTrue(sampleDataValue1, condition.operator, operand2.value);
            }
            else {
                sampleDataValue2 = extractSampleDataValue(operand2, sampleData);
                return isConditionTrue(operand1.value, condition.operator, sampleDataValue2);
            }
        }
        return true;
    }

    private boolean isConditionTrue(Object value1, String operator, Object value2) {
        return switch (operator) {
            case "=" -> isEqual(value1, value2);
            case "!=" -> !isEqual(value1, value2);
            case ">" -> compare(value1, value2) > 0;
            case ">=" -> compare(value1, value2) >= 0;
            case "<" -> compare(value1, value2) < 0;
            case "<=" -> compare(value1, value2) <= 0;
            case "include" -> ((List<Object>)value1).contains(value2);
            default-> false;
        };
    }

    private int compare(Object value1, Object value2) {
        if (value1 instanceof Integer int1 && value2 instanceof Integer int2)
            return Integer.compare(int1, int2);
        else if (value1 instanceof Double double1 && value2 instanceof Double double2)
            return Double.compare(double1, double2);
        else if (value1 instanceof Integer int1 && value2 instanceof Double double2)
            return Double.compare(int1.doubleValue(), double2);
        else if (value1 instanceof Double double1 && value2 instanceof Integer int2)
            return Double.compare(double1, int2.doubleValue());
        else if (value1 instanceof LocalDateTime time1 && value2 instanceof LocalDateTime time2)
            return time1.compareTo(time2);
        else
            throw new IllegalArgumentException("Error: Wrong types: " + value1.getClass() + " and " + value2.getClass());
    }

    private boolean isEqual(Object value1, Object value2) {
        if (value1 == null || value2 == null)
            return value1 == value2;
        if ((value1 instanceof Number) && (value2 instanceof Number)) {
            return compare(value1, value2) == 0;
        }
        return value1.equals(value2);
    }

    private Object extractSampleDataValue(Operand operand, HashMap<String,Object> sampleData) {
        String value = ((String)operand.value).trim();
        if (value.contains(".")) {
            String[] subValue = value.split("\\.(?=[a-zA-Z])");
            if (subValue.length != 1) {
                Operand subOperand = new Operand(operand.type, subValue[1]);
                return extractSampleDataValue(subOperand, (HashMap<String,Object>)sampleData.get(subValue[0]));
            }
        }

        if (value.contains("+") || value.contains("-")) {
            double result = 0;
            Pattern pattern = Pattern.compile("([+-]?\\s*[a-zA-Z_][a-zA-Z0-9_]*|[+-]?\\s*\\d+(\\.\\d+)?)");
            Matcher matcher = pattern.matcher(value.trim());

            while (matcher.find()) {
                String valuePart = matcher.group().replaceAll("\\s+", "");
                try {
                    result += Double.parseDouble(valuePart);
                } catch (NumberFormatException e) {
                    Number variableNumber = (Number) sampleData.get(valuePart.replaceFirst("^[+-]", ""));
                    double variableValue = variableNumber.doubleValue();
                    if (valuePart.startsWith("-"))
                        result -= variableValue;
                    else
                        result += variableValue;
                }
            }
            return result;
        }

        return sampleData.get(value);
    }
}
