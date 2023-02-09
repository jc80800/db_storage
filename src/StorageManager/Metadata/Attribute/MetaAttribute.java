package StorageManager.Metadata.Attribute;

import Constants.Constant;
import Constants.Constant.DataType;
import Constants.Helper;
import java.util.Arrays;

public class MetaAttribute {

    private final String name;
    private final DataType type;
    private final boolean isPrimaryKey;

    // byte: length of name, string name, number of length of type, type

    public MetaAttribute(String name, DataType type, boolean isPrimaryKey) {
        this.name = name;
        this.type = type;
        this.isPrimaryKey = isPrimaryKey;
    }

    /**
     * Construct metaAttribute object from the byte array in the following form:
     * [isPrimary(boolean), nameLength(int), name(String), typeLength(int),typeString(String)]
     *
     * @param bytes - binary form of metaAttribute
     * @return metaAttribute object
     */
    public MetaAttribute deserialize(byte[] bytes) {
        int index = 0;
        boolean isPrimaryKey = Helper.convertByteToBoolean(bytes[index++]);

        int nameLength = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE + 1));
        index += Constant.INTEGER_SIZE + 1;
        String name = Helper.convertByteArrayToString(
            Arrays.copyOfRange(bytes, index, index + nameLength + 1));
        index += nameLength + 1;

        int typeLength = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE + 1));
        index += Constant.INTEGER_SIZE + 1;
        String typeString = Helper.convertByteArrayToString(
            Arrays.copyOfRange(bytes, index, index + typeLength + 1));
        DataType type = switch (typeString) {
            case Constant.INTEGER -> DataType.INTEGER;
            case Constant.BOOLEAN -> DataType.BOOLEAN;
            case Constant.DOUBLE -> DataType.DOUBLE;
        };
        return new MetaAttribute(name, type, isPrimaryKey);
    }

    /**
     * serialize metaAttribute into the following form in bytes [isPrimary(boolean),
     * nameLength(int), name(String), typeLength(int),typeString(String)];
     *
     * @param metaAttribute - metaAttribute
     * @return
     */
    public byte[] serialize(MetaAttribute metaAttribute) {
        byte[] isPrimaryKeyBytes = new byte[]{
            Helper.convertBooleanToByte(metaAttribute.getIsPrimaryKey())};
        String name = metaAttribute.getName();
        int nameLength = name.length();
        byte[] nameLengthBytes = Helper.convertIntToByteArray(nameLength);
        byte[] nameBytes = Helper.convertStringToByteArrays(name);

        DataType type = metaAttribute.getType();
        String typeString = getTypeString(type);
        int typeLength = typeString.length();
        byte[] typeLengthBytes = Helper.convertIntToByteArray(typeLength);
        byte[] typeStringBytes = Helper.convertStringToByteArrays(typeString);
        return Helper.concatenate(isPrimaryKeyBytes, nameLengthBytes, nameBytes, typeLengthBytes,
            typeStringBytes);
    }


    private String getTypeString(DataType type) {
        return switch (type) {
            case INTEGER -> Constant.INTEGER;
            case DOUBLE -> Constant.DOUBLE;
            case BOOLEAN -> Constant.BOOLEAN;
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
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
}
