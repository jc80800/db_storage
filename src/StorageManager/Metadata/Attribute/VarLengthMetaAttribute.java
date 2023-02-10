package StorageManager.Metadata.Attribute;

import Constants.Constant;
import Constants.Constant.DataType;
import Constants.Helper;
import java.util.Arrays;

public class VarLengthMetaAttribute extends FixedLengthMetaAttribute implements MetaAttribute {

    // length of type (CHAR, VARCHAR);
    private final int length;

    public VarLengthMetaAttribute(String name, Constant.DataType type, Boolean isPrimaryKey,
        int length) {
        super(name, type, isPrimaryKey);
        this.length = length;
    }

    /**
     * serialize varLengthMetaAttribute into the following form in bytes [isPrimary(boolean),
     * nameLength(int), name(String), typeLength(int),typeString(String), valueLength(int)];
     *
     * @param varLengthMetaAttribute - varLengthMetaAttribute
     * @return byte arrays
     */
    public byte[] serialize(VarLengthMetaAttribute varLengthMetaAttribute) {
        byte[] isPrimaryKeyBytes = new byte[]{
            Helper.convertBooleanToByte(varLengthMetaAttribute.getIsPrimaryKey())};
        String name = varLengthMetaAttribute.getName();
        int nameLength = name.length();
        byte[] nameLengthBytes = Helper.convertIntToByteArray(nameLength);
        byte[] nameBytes = Helper.convertStringToByteArrays(name);

        DataType type = varLengthMetaAttribute.getType();
        String typeString = getTypeString(type);
        int typeLength = typeString.length();
        byte[] typeLengthBytes = Helper.convertIntToByteArray(typeLength);
        byte[] typeStringBytes = Helper.convertStringToByteArrays(typeString);
        byte[] valueLength = Helper.convertIntToByteArray(varLengthMetaAttribute.getLength());
        return Helper.concatenate(isPrimaryKeyBytes, nameLengthBytes, nameBytes, typeLengthBytes,
            typeStringBytes, valueLength);
    }

    /**
     * Construct metaAttribute object from the byte array in the following form:
     * [isPrimary(boolean), nameLength(int), name(String), typeLength(int), typeString(String),
     * valueLength(int)]
     *
     * @param bytes - binary form of metaAttribute
     * @return metaAttribute object
     */
    @Override
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
        index += typeLength + 1;
        DataType type = switch (typeString) {
            case Constant.CHAR -> DataType.CHAR;
            case Constant.VARCHAR -> DataType.VARCHAR;
            default -> DataType.VARCHAR;
        };
        int valueLength = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE + 1));

        return new VarLengthMetaAttribute(name, type, isPrimaryKey, valueLength);
    }

    @Override
    protected String getTypeString(DataType type) {
        return switch (type) {
            case CHAR -> Constant.CHAR;
            case VARCHAR -> Constant.VARCHAR;
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    public int getLength() {
        return length;
    }
}
