package StorageManager.Metadata;

import Constants.Constant;
import Constants.Constant.DataType;
import Constants.Helper;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Map.Entry;

public class MetaAttribute {

    private final boolean isPrimaryKey;
    private final String name;
    private final DataType type;
    private final Integer length;
    private final int binarySize;

    public MetaAttribute(boolean isPrimaryKey, String name, DataType type) {
        this.isPrimaryKey = isPrimaryKey;
        this.name = name;
        this.type = type;
        this.length = null;
        this.binarySize = calculateBinarySize();
    }

    public MetaAttribute(boolean isPrimaryKey, String name, DataType type, Integer length) {
        this.isPrimaryKey = isPrimaryKey;
        this.name = name;
        this.type = type;
        this.length = length;
        this.binarySize = calculateBinarySize();
    }

    public MetaAttribute(boolean isPrimaryKey, String name, DataType type, Integer length, int binarySize) {
        this.isPrimaryKey = isPrimaryKey;
        this.name = name;
        this.type = type;
        this.length = length;
        this.binarySize = binarySize;
    }


    public int calculateBinarySize() {
        int size = Constant.BOOLEAN_SIZE;
        size += Constant.INTEGER_SIZE;
        size += name.getBytes().length;
        size += Constant.INTEGER_SIZE;
        size += Constant.BOOLEAN_SIZE;
        if (length != null) {
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
        String name = getName();
        int nameLength = name.length();
        byte[] nameLengthBytes = Helper.convertIntToByteArray(nameLength);
        byte[] nameBytes = Helper.convertStringToByteArrays(name);

        Integer typeCode;
        try {
            typeCode = getDataTypeCode(getType());
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        byte[] typeCodeBytes = Helper.convertIntToByteArray(typeCode);
        boolean isLength = getLength() != null;
        byte[] isLengthBytes = new byte[]{
            Helper.convertBooleanToByte(isLength)};
        if (isLength) {
            byte[] lengthBytes = Helper.convertIntToByteArray(getLength());
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
            Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE));
        index += Constant.INTEGER_SIZE;
        String name = Helper.convertByteArrayToString(
            Arrays.copyOfRange(bytes, index, index + nameLength));
        index += nameLength;

        int dataTypeCode = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE));
        index += Constant.INTEGER_SIZE;
        DataType dataType = getDataType(dataTypeCode);

        boolean isLength = Helper.convertByteToBoolean(bytes[index++]);
        if (isLength) {
            int length = Helper.convertByteArrayToInt(
                Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE));
            return new MetaAttribute(isPrimaryKey, name, dataType, length, bytes.length);
        }
        return new MetaAttribute(isPrimaryKey, name, dataType, bytes.length);
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

    public Integer getLength() {
        return length;
    }

    public int getBinarySize() {
        return binarySize;
    }

    @Override
    public String toString() {
        return "MetaAttribute{" +
            "isPrimaryKey=" + isPrimaryKey +
            ", name='" + name + '\'' +
            ", type=" + type +
            ", length=" + length +
            ", binarySize=" + binarySize +
            '}';
    }
}
