package StorageManager.Metadata;

import Constants.Constant;
import Constants.Helper;
import StorageManager.Coordinate;
import java.util.ArrayList;
import java.util.Arrays;

public final class MetaTable {

    private final String tableName;
    private final ArrayList<MetaAttribute> metaAttributes;
    private final ArrayList<Coordinate> pointers;

    public MetaTable(String tableName,
        ArrayList<MetaAttribute> metaAttributes) {
        this.tableName = tableName;
        this.metaAttributes = metaAttributes;
        pointers = constructPointers();
    }

    public MetaTable(String tableName, ArrayList<MetaAttribute> metaAttributes,
        ArrayList<Coordinate> pointers) {
        this.tableName = tableName;
        this.metaAttributes = metaAttributes;
        this.pointers = pointers;
    }

    /**
     * form: [nameLength(int), name(String), #ofAttributes(int), list of coordinates(coordinate),
     * list of attributes(metaAttribute)]
     *
     * @return MetaTable
     */
    public static MetaTable deserialize(byte[] bytes) {
        int index = 0;
        int nameLength = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE + 1));
        index += Constant.INTEGER_SIZE + 1;
        String name = Helper.convertByteArrayToString(
            Arrays.copyOfRange(bytes, index, nameLength + 1));
        index += nameLength + 1;

        int numOfMetaAttributes = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE + 1));
        index += Constant.INTEGER_SIZE + 1;

        ArrayList<MetaAttribute> metaAttributes = new ArrayList<>();
        ArrayList<Coordinate> pointers = new ArrayList<>();

        while (numOfMetaAttributes > 0) {
            Coordinate coordinate = Coordinate.deserialize(
                Arrays.copyOfRange(bytes, index, index + (Constant.INTEGER_SIZE * 2) + 1));
            pointers.add(coordinate);
            index += (Constant.INTEGER_SIZE * 2) + 1;
            MetaAttribute metaAttribute = MetaAttribute.deserialize(
                Arrays.copyOfRange(bytes, coordinate.getOffset(), coordinate.getLength() + 1));
            metaAttributes.add(metaAttribute);
            numOfMetaAttributes--;
        }
        return new MetaTable(name, metaAttributes, pointers);
    }

    /**
     * form: [nameLength(int), name(String), #ofAttributes(int), list of coordinates(Coordinate),
     * list of attributes(metaAttribute)]
     *
     * @return byte array
     */
    public byte[] serialize() {
        byte[] nameLengthBytes = Helper.convertIntToByteArray(tableName.length());
        byte[] nameBytes = Helper.convertStringToByteArrays(tableName);
        byte[] numOfMetaAttributes = Helper.convertIntToByteArray(metaAttributes.size());

        byte[] pointersBytes = new byte[0];
        for (Coordinate pointer : pointers) {
            byte[] pointerBytes = pointer.serialize();
            Helper.concatenate(pointersBytes, pointerBytes);
        }

        byte[] metaAttributesBytes = new byte[0];
        for (MetaAttribute metaAttribute : metaAttributes) {
            byte[] metaAttributeBytes = metaAttribute.serialize();
            metaAttributesBytes = Helper.concatenate(metaAttributesBytes, metaAttributeBytes);
        }

        return Helper.concatenate(nameLengthBytes, nameBytes, numOfMetaAttributes, pointersBytes,
            metaAttributesBytes);
    }

    private ArrayList<Coordinate> constructPointers() {
        ArrayList<Coordinate> pointers = new ArrayList<>();
        byte[] nameLengthBytes = Helper.convertIntToByteArray(tableName.length());
        byte[] nameBytes = Helper.convertStringToByteArrays(tableName);
        byte[] numOfMetaAttributes = Helper.convertIntToByteArray(metaAttributes.size());
        int offset = nameLengthBytes.length + nameBytes.length + numOfMetaAttributes.length;
        for (MetaAttribute metaAttribute : metaAttributes) {
            byte[] metaAttributeBytes = metaAttribute.serialize();
            pointers.add(new Coordinate(offset, metaAttributeBytes.length));
            offset += metaAttributeBytes.length + 1;
        }
        return pointers;
    }

    public String tableName() {
        return tableName;
    }

    public ArrayList<MetaAttribute> metaAttributes() {
        return metaAttributes;
    }
}
