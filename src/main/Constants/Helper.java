package main.Constants;

import java.nio.ByteBuffer;
import java.util.Arrays;


public class Helper {

    public static byte[] convertIntToByteArray(int v) {
        return ByteBuffer.allocate(4).putInt(v).array();
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
        return ByteBuffer.allocate(8).putDouble(d).array();
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

    public static void fillSubArray(byte[] array, int start, int end, byte[] subArray) {
        for (int i = 0; start < end; start++, i++) {
            array[start] = subArray[i];
        }
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
}
