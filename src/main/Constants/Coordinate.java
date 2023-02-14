package main.Constants;

import java.util.Arrays;
import java.util.Objects;

public class Coordinate {

    private int offset;
    private int length;

    public Coordinate(int offset, int length) {
        this.offset = offset;
        this.length = length;
    }

    public byte[] serialize() {
        byte[] offsetBytes = Helper.convertIntToByteArray(offset);
        byte[] lengthBytes = Helper.convertIntToByteArray(length);
        return Helper.concatenate(offsetBytes, lengthBytes);
    }

    public static Coordinate deserialize(byte[] bytes) {
        int index = 0;
        int offset = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE));
        index += Constant.INTEGER_SIZE;
        int length = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE));
        return new Coordinate(offset, length);
    }

    public static int getBinarySize() {
        return Constant.INTEGER_SIZE * 2;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "Coordinate{" +
            "offset=" + offset +
            ", length=" + length +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Coordinate that = (Coordinate) o;
        return offset == that.offset && length == that.length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, length);
    }
}
