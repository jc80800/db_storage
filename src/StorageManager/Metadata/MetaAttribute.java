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

    public MetaAttribute(boolean isPrimaryKey, String name, DataType type) {
        this.isPrimaryKey = isPrimaryKey;
        this.name = name;
        this.type = type;
        this.length = null;
    }

    public MetaAttribute(boolean isPrimaryKey, String name, DataType type, Integer length) {
        this.isPrimaryKey = isPrimaryKey;
        this.name = name;
        this.type = type;
        this.length = length;
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
            Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE + 1));
        index += Constant.INTEGER_SIZE + 1;
        String name = Helper.convertByteArrayToString(
            Arrays.copyOfRange(bytes, index, index + nameLength + 1));
        index += nameLength + 1;

        int dataTypeCode = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE + 1));
        index += Constant.INTEGER_SIZE + 1;
        DataType dataType = getDataType(dataTypeCode);

        boolean isLength = Helper.convertByteToBoolean(bytes[index++]);
        if (isLength) {
            int length = Helper.convertByteArrayToInt(
                Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE + 1));
            return new MetaAttribute(isPrimaryKey, name, dataType, length);
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

    public Integer getLength() {
        return length;
    }
}
