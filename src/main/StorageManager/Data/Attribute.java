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

    public Attribute(MetaAttribute metaAttribute, String value) {
        this.metaAttribute = metaAttribute;
        setValue(value);
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
        if (value == null) {
            return 0;
        }
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

    public void setValue(String newValue) {
        if (newValue == null) {
            this.value = null;
        } else {
            Constant.DataType dataType = metaAttribute.getType();
            switch (dataType) {
                case INTEGER -> {
                    try {
                        this.value = Integer.parseInt(newValue);
                    } catch (NumberFormatException e) {
                        System.out.printf("Invalid value: \"%s\" for Integer Type\n", newValue);
                        throw new IllegalArgumentException();
                    }
                }
                case DOUBLE -> {
                    try {
                        this.value = Double.parseDouble(newValue);
                    } catch (NumberFormatException e) {
                        System.out.printf("Invalid value: \"%s\" for Double Type\n", newValue);
                        throw new IllegalArgumentException();
                    }
                }
                case BOOLEAN -> {
                    if (newValue.equalsIgnoreCase("true")) {
                        this.value = true;
                    } else if (newValue.equalsIgnoreCase("false")) {
                        this.value = false;
                    } else {
                        System.out.printf("Invalid value: \"%s\" for Boolean Type\n", newValue);
                        throw new IllegalArgumentException();
                    }
                }
                case CHAR, VARCHAR -> {
                    if (newValue.charAt(0) != '\"'
                            || newValue.charAt(newValue.length() - 1) != '\"') {
                        System.out.printf("Invalid value: %s, missing quotes\n", newValue);
                        throw new IllegalArgumentException();
                    }
                    newValue = newValue.substring(1, newValue.length() - 1);
                    if (newValue.length() > metaAttribute.getMaxLength()) {
                        System.out.printf("\"%s\" length exceeds %s(%d)\n", newValue,
                                dataType.name(), metaAttribute.getMaxLength());
                        throw new IllegalArgumentException();
                    }
                    this.value = newValue;
                }
            }
        }
        this.binarySize = calculateBinarySize();
    }

    public MetaAttribute getMetaAttribute() {
        return metaAttribute;
    }

    public boolean checkPrimaryKey() {
        return metaAttribute.getIsPrimaryKey();
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
        return binarySize == attribute.binarySize && metaAttribute.equals(attribute.metaAttribute)
            && Objects.equals(value, attribute.value);
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
