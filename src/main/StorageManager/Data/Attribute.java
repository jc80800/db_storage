package main.StorageManager.Data;

import java.util.Arrays;
import java.util.Objects;
import main.Constants.Constant;
import main.Constants.Helper;
import main.StorageManager.MetaData.MetaAttribute;

public class Attribute {

    private final MetaAttribute metaAttribute;
    private Object value;
    private int binarySize;

    public Attribute(MetaAttribute metaAttribute, Object value) {
        this.metaAttribute = metaAttribute;
        this.value = value;
        this.binarySize = calculateBinarySize();
    }

    public Attribute(MetaAttribute metaAttribute, Object value, int binarySize) {
        this.metaAttribute = metaAttribute;
        this.value = value;
        this.binarySize = binarySize;
    }

    public static Attribute deserialize(byte[] bytes, MetaAttribute metaAttribute) {
        int index = 0;
        Object value;
        value = switch (metaAttribute.getType()) {
            case BOOLEAN -> Helper.convertByteToBoolean(bytes[index]);
            case INTEGER -> Helper.convertByteArrayToInt(bytes);
            case DOUBLE -> Helper.convertByteArrayToDouble(bytes);
            // CHAR: [len, xxxx, 0, 0...] xxx is actual data, the rest 0's are padding
            case CHAR -> {
                int stringLen = Helper.convertByteArrayToInt(Arrays.copyOf(bytes, Constant.INTEGER_SIZE));
                index += Constant.INTEGER_SIZE;
                yield Helper.convertByteArrayToString(
                    Arrays.copyOfRange(bytes, index, index + stringLen));
            }
            case VARCHAR -> Helper.convertByteArrayToString(bytes);
        };
        return new Attribute(metaAttribute, value);
    }

    public byte[] serialize() {
        return switch (metaAttribute.getType()) {
            case BOOLEAN -> new byte[]{Helper.convertBooleanToByte((Boolean) value)};
            case INTEGER -> Helper.convertIntToByteArray((int) value);
            case DOUBLE -> Helper.convertDoubleToByteArray((double) value);
            // CHAR: [len, xxxx, 0, 0...] xxx is actual data, the rest 0's are padding
            case CHAR -> {
                byte[] valueBytes = Helper.convertStringToByteArrays((String) value);
                byte[] valueLength = Helper.convertIntToByteArray(valueBytes.length);
                // padding with 0 to CHAR maxLength;
                valueBytes = Arrays.copyOf(valueBytes, metaAttribute.getMaxLength());
                yield Helper.concatenate(valueLength, valueBytes);
            }
            case VARCHAR -> Helper.convertStringToByteArrays((String) value);
        };
    }

    private int calculateBinarySize() {
        return switch (metaAttribute.getType()) {
            case BOOLEAN -> Constant.BOOLEAN_SIZE;
            case INTEGER -> Constant.INTEGER_SIZE;
            case DOUBLE -> Constant.DOUBLE_SIZE;
            case CHAR -> Constant.INTEGER_SIZE + metaAttribute.getMaxLength();
            case VARCHAR -> Helper.convertStringToByteArrays((String) value).length;
        };
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        this.binarySize = calculateBinarySize();
    }

    public MetaAttribute getMetaAttribute() {
        return metaAttribute;
    }

    public boolean checkPrimaryKey(){
        if (metaAttribute.getIsPrimaryKey()){
            return true;
        }
        return false;
    }

    public int getBinarySize() {
        return binarySize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Attribute attribute = (Attribute) o;
        return metaAttribute.equals(attribute.metaAttribute) && value.equals(attribute.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metaAttribute, value);
    }

    @Override
    public String toString() {
        return "Attribute{" +
            "metaAttribute=" + metaAttribute +
            ", value=" + value +
            '}';
    }
}
