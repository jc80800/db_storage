package main.StorageManager.MetaData;

import main.Constants.Constant;
import main.Constants.Constant.DataType;
import main.Constants.Helper;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Objects;

public class MetaAttribute {

    private final boolean isPrimaryKey;
    private final String name;
    private final DataType type;
    private final Integer maxLength;
    private final int binarySize;

    public MetaAttribute(boolean isPrimaryKey, String name, DataType type) {
        this.isPrimaryKey = isPrimaryKey;
        this.name = name;
        this.type = type;
        this.maxLength = null;
        this.binarySize = calculateBinarySize();
    }

    public MetaAttribute(boolean isPrimaryKey, String name, DataType type, Integer maxLength) {
        this.isPrimaryKey = isPrimaryKey;
        this.name = name;
        this.type = type;
        this.maxLength = maxLength;
        this.binarySize = calculateBinarySize();
    }

    public MetaAttribute(boolean isPrimaryKey, String name, DataType type, Integer maxLength, int binarySize) {
        this.isPrimaryKey = isPrimaryKey;
        this.name = name;
        this.type = type;
        this.maxLength = maxLength;
        this.binarySize = binarySize;
    }


    public int calculateBinarySize() {
        int size = Constant.BOOLEAN_SIZE;
        size += Constant.INTEGER_SIZE;
        size += name.getBytes().length;
        size += Constant.INTEGER_SIZE;
        size += Constant.BOOLEAN_SIZE;
        if (maxLength != null) {
            size += Constant.INTEGER_SIZE;
        }
        return size;
    }

    /**
     * serialize metaAttribute into the following form in bytes [isPrimary(boolean),
     * nameLength(int), name(String), DataTypeCode(int), isLength(bool), {length(int) if isLength is
     * true}];
     *
     * @return byte arrays
     */
    public byte[] serialize() {
        byte[] isPrimaryKeyBytes = new byte[]{
            Helper.convertBooleanToByte(getIsPrimaryKey())};
        byte[] nameBytes = Helper.convertStringToByteArrays(name);
        int nameLength = nameBytes.length;
        byte[] nameLengthBytes = Helper.convertIntToByteArray(nameLength);

        Integer typeCode;
        try {
            typeCode = getDataTypeCode(getType());
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        byte[] typeCodeBytes = Helper.convertIntToByteArray(typeCode);
        boolean isLength = getMaxLength() != null;
        byte[] isLengthBytes = new byte[]{
            Helper.convertBooleanToByte(isLength)};
        if (isLength) {
            byte[] lengthBytes = Helper.convertIntToByteArray(getMaxLength());
            return Helper.concatenate(isPrimaryKeyBytes, nameLengthBytes, nameBytes, typeCodeBytes,
                isLengthBytes, lengthBytes);
        }
        return Helper.concatenate(isPrimaryKeyBytes, nameLengthBytes, nameBytes, typeCodeBytes,
            isLengthBytes);
    }

    /**
     * Construct metaAttribute object from the byte array in the following form:
     * [isPrimary(boolean), nameLength(int), name(String), DataTypeCode(int), isLength(bool),
     * {length(int) if isLength is true}];
     *
     * @param bytes - binary form of metaAttribute
     * @return metaAttribute object
     */
    public static MetaAttribute deserialize(byte[] bytes) {
        int index = 0;
        boolean isPrimaryKey = Helper.convertByteToBoolean(bytes[index++]);

        int nameLength = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index += Constant.INTEGER_SIZE));
        String name = Helper.convertByteArrayToString(
            Arrays.copyOfRange(bytes, index, index += nameLength));

        int dataTypeCode = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index += Constant.INTEGER_SIZE));
        DataType dataType = getDataType(dataTypeCode);

        boolean isLength = Helper.convertByteToBoolean(bytes[index++]);
        if (isLength) {
            int length = Helper.convertByteArrayToInt(
                Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE));
            return new MetaAttribute(isPrimaryKey, name, dataType, length, bytes.length);
        }
        return new MetaAttribute(isPrimaryKey, name, dataType);
    }

    private static DataType getDataType(int code) {
        return Constant.DATA_TYPE_MAP.get(code);
    }

    private Integer getDataTypeCode(DataType dataType) throws InvalidKeyException {
        for (Entry<Integer, DataType> entry : Constant.DATA_TYPE_MAP.entrySet()) {
            if (dataType == entry.getValue()) {
                return entry.getKey();
            }
        }
        throw new InvalidKeyException();
    }

    public String getName() {
        return name;
    }

    public Constant.DataType getType() {
        return type;
    }

    public boolean getIsPrimaryKey() {
        return isPrimaryKey;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public int getBinarySize() {
        return binarySize;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name + ":");
        if (type == DataType.CHAR || type == DataType.VARCHAR) {
            sb.append(type.name() + "(" + maxLength + ")");
        } else {
            sb.append(type.name());
        }
        if (isPrimaryKey) {
            sb.append(" primarykey");
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetaAttribute that = (MetaAttribute) o;
        return isPrimaryKey == that.isPrimaryKey && binarySize == that.binarySize && name.equals(
            that.name) && type == that.type && Objects.equals(maxLength, that.maxLength);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isPrimaryKey, name, type, maxLength, binarySize);
    }
}
