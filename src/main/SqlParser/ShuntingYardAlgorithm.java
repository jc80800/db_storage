package main.SqlParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Stack;
import main.Constants.Constant.DataType;
import main.StorageManager.Data.Attribute;
import main.StorageManager.Data.Record;
import main.StorageManager.MetaData.MetaAttribute;

public class ShuntingYardAlgorithm {

    public static Queue<String> parse(String expression) {
        if (expression == null) {
            return new LinkedList<>();
        }
        // split on space except in quotes
        String[] tokens = expression.split("(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)\\s+");
        Stack<String> operators = new Stack<>();
        Queue<String> output = new LinkedList<>();

        for (String token : tokens) {
            // remove quotes
            token = token.replaceAll("\"", "");
            if (isOperator(token)) {
                while (!operators.empty() && checkPrecedence(operators.peek(), token)) {
                    output.add(operators.pop());
                }
                operators.push(token);
            } else {
                output.add(token);
            }
        }

        while (!operators.empty()) {
            output.add(operators.pop());
        }
        return output;
    }

    public static Boolean evaluate(Queue<String> postfix, Record record) throws IllegalArgumentException{
        if (postfix.isEmpty()) {
            return true;
        }
        Stack<Boolean> resultStack = new Stack<>();
        Stack<String> valueStack = new Stack<>();
        while (!postfix.isEmpty()) {
            String token = postfix.poll();
            if (token.equalsIgnoreCase("and")) {
                boolean right = resultStack.pop();
                boolean left = resultStack.pop();
                resultStack.push(left && right);
            } else if (token.equalsIgnoreCase("or")) {
                boolean right = resultStack.pop();
                boolean left = resultStack.pop();
                resultStack.push(left || right);
            } else if (isOperator(token)) {
                String right = valueStack.pop();
                String left = valueStack.pop();
                boolean result = Objects.requireNonNull(
                    evaluateCondition(record, token, left, right));
                resultStack.push(result);
            } else {
                valueStack.push(token);
            }
        }
        return resultStack.pop();
    }

    private static Boolean evaluateCondition(Record record, String operator, String attributeName,
        String value) {

        // Check if the attributeName is without .extension by checking inside the record
        ArrayList<Attribute> tempAtt = record.getAttributes();
        boolean found = false;
        Attribute attribute = null;
        for(Attribute attribute1 : tempAtt){
            if(attribute1.getMetaAttribute().getName().equals(attributeName)){
                // check if that attribute is duplicate
                if(record.checkDuplicateAttribute(attributeName)){
                    System.out.printf("Need to specify Duplicate attribute %s\n", attributeName);
                    throw new IllegalArgumentException();
                } else{
                    attribute = attribute1;
                    found = true;
                    break;
                }
            }
        }
        if (!found){
            attribute = record.getAttributeByName(attributeName);
        }
        if (attribute == null) {
            System.out.printf("No such attribute %s\n", attributeName);
            throw new IllegalArgumentException();
        }
        DataType type = attribute.getMetaAttribute().getType();
        if(attribute.getValue() == null){
            switch(operator){
                case "=" -> {
                    return value.equals("null");
                }
                case "!=" -> {
                    return !value.equals("null");
                }
                default -> {
                    return false;
                }
            }
        }

        switch (type) {
            case INTEGER -> {
                int intValue;
                try {
                    intValue = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    System.out.printf("%s expects datatype %s, but received %s\n", attributeName,
                        type,
                        value);
                    throw e;
                }
                int attributeValue = (int) attribute.getValue();
                switch (operator) {
                    case "=" -> {
                        return attributeValue == intValue;
                    }
                    case ">" -> {
                        return attributeValue > intValue;
                    }
                    case "<" -> {
                        return attributeValue < intValue;
                    }
                    case "<=" -> {
                        return attributeValue <= intValue;
                    }
                    case ">=" -> {
                        return attributeValue >= intValue;
                    }
                    case "!=" -> {
                        return attributeValue != intValue;
                    }
                    default -> {
                        System.out.printf("operator %s is not supported for type %s\n", operator,
                            type);
                        throw new IllegalArgumentException();
                    }
                }
            }
            case DOUBLE -> {
                double doubleValue;
                try {
                    doubleValue = Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    System.out.printf("%s expects datatype %s, but received %s", attributeName,
                        type,
                        value);
                    throw e;
                }
                double attributeValue = (double) attribute.getValue();
                switch (operator) {
                    case "=" -> {
                        return attributeValue == doubleValue;
                    }
                    case ">" -> {
                        return attributeValue > doubleValue;
                    }
                    case "<" -> {
                        return attributeValue < doubleValue;
                    }
                    case "<=" -> {
                        return attributeValue <= doubleValue;
                    }
                    case ">=" -> {
                        return attributeValue >= doubleValue;
                    }
                    case "!=" -> {
                        return attributeValue != doubleValue;
                    }
                    default -> {
                        System.out.printf("operator %s is not supported for type %s\n", operator,
                            type);
                        throw new IllegalArgumentException();
                    }
                }
            }
            case BOOLEAN -> {
                boolean boolValue = value.equalsIgnoreCase("true");
                boolean attributeValue = (boolean) attribute.getValue();
                if (operator.equals("=")) {
                    return attributeValue == boolValue;
                } else if (operator.equals("!=")) {
                    return attributeValue != boolValue;
                } else {
                    System.out.printf("operator %s is not supported for type %s\n", operator,
                        type);
                    throw new IllegalArgumentException();
                }
            }
            case CHAR, VARCHAR -> {
                String attributeValue = (String) attribute.getValue();
                if (operator.equals("=")) {
                    return attributeValue.equals(value);
                } else if (operator.equals("!=")) {
                    return !attributeValue.equals(value);
                } else if (operator.equals(">")) {
                    return attributeValue.compareTo(value) > 0;
                } else if (operator.equals("<")) {
                    return attributeValue.compareTo(value) < 0;
                } else if (operator.equals(">=")) {
                    return attributeValue.compareTo(value) >= 0;
                } else if (operator.equals("<=")) {
                    return attributeValue.compareTo(value) <= 0;
                } else {
                    System.out.printf("operator %s is not supported for type %s\n", operator,
                        type);
                    throw new IllegalArgumentException();
                }
            }
        }
        return null;
    }

    private static boolean checkPrecedence(String operator1, String operator2) {
        HashMap<String, Integer> precedence = new HashMap<>();
        precedence.put("and", 2);
        precedence.put("or", 1);
        precedence.put("=", 3);
        precedence.put(">", 3);
        precedence.put("<", 3);
        precedence.put(">=", 3);
        precedence.put("<=", 3);
        precedence.put("!=", 3);

        return precedence.get(operator1.toLowerCase()) > precedence.get(operator2.toLowerCase());
    }

    private static boolean isOperator(String token) {
        return token.equals("=") || token.equals("!=") || token.equals(">") || token.equals("<")
            || token.equals(">=") || token.equals("<=") || token.equalsIgnoreCase("and")
            || token.equalsIgnoreCase("or");
    }
}
