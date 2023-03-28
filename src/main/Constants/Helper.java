package main.Constants;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;


public class Helper {

    public static byte[] convertIntToByteArray(int v) {
        return ByteBuffer.allocate(Constant.INTEGER_SIZE).putInt(v).array();
    }

    public static int convertByteArrayToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static byte convertBooleanToByte(boolean bool) {
        return (byte) (bool ? 1 : 0);
    }

    public static boolean convertByteToBoolean(byte b) {
        return b != 0;
    }

    public static byte[] convertDoubleToByteArray(double d) {
        return ByteBuffer.allocate(Constant.DOUBLE_SIZE).putDouble(d).array();
    }

    public static double convertByteArrayToDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

    public static byte[] convertStringToByteArrays(String s) {
        return s.getBytes(Constant.CHARSET);
    }

    public static String convertByteArrayToString(byte[] bytes) {
        return new String(bytes, Constant.CHARSET);
    }

    public static boolean checkInteger(String s){
        try {
            int i = Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }

    public static boolean checkDouble(String s){
        try {
            double i = Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }

    public static boolean checkBoolean(String s){
        return s.equals("true") || s.equals("false");
    }



    /**
     * concatenate an array of byte arrays into a single array in the given order.
     *
     * @param arrays - array of byte arrays
     * @return concatenated byte array
     */
    public static byte[] concatenate(byte[]... arrays) {
        int finalLength = 0;
        for (byte[] array : arrays) {
            finalLength += array.length;
        }

        byte[] dest = null;
        int destPos = 0;

        for (byte[] array : arrays) {
            if (dest == null) {
                dest = Arrays.copyOf(array, finalLength);
                destPos = array.length;
            } else {
                System.arraycopy(array, 0, dest, destPos, array.length);
                destPos += array.length;
            }
        }
        return dest;
    }

    public static String concatString(String[] field, int startIndex, int endIndex){
        ArrayList<String> result = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i++){
            result.add(field[i]);
        }
        return String.join(" ", result);
    }

    public static String[] implementShuntingYard(String expression){
        expression = "a = 6 and b = 6 or c = 3 and d = 8";
        String[] tokens = expression.split(" ");
        Stack<String> stack = new Stack<>();
        ArrayList<String> queue = new ArrayList<>();

        for(String token : tokens){
            if(checkOperator(token)){
                while(!stack.empty() && checkPrecedence(stack.peek(), token)){
                    queue.add(stack.pop());
                }
                stack.push(token);
            } else {
                queue.add(token);
            }
        }

        while(!stack.empty()){
            queue.add(stack.pop());
        }
        System.out.println(queue);
        return null;
    }

    public static boolean checkPrecedence(String operator1, String operator2){
        HashMap<String, Integer> precedence = new HashMap<String, Integer>();
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

    public static boolean checkOperator(String token){
        Set<String> operators = new HashSet<String>();
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
