package main.SqlParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class ShuntingYardAlgorithm {

    public static Queue<String> parse(String expression) {
        String[] tokens = expression.split(" ");
        Stack<String> operators = new Stack<>();
        Queue<String> output = new LinkedList<>();

        for (String token : tokens) {
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

        return precedence.get(operator1) > precedence.get(operator2);
    }

    private static boolean isOperator(String token) {
        Set<String> operators = new HashSet<>();
        operators.add("and");
        operators.add("or");
        operators.add("=");
        operators.add(">");
        operators.add("<");
        operators.add(">=");
        operators.add("<=");
        operators.add("!=");

        return operators.contains(token);
    }
}
