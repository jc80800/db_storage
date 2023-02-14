package main.StorageManager.MetaData;

import main.Constants.Constant;
import main.Constants.Helper;
import main.Constants.Coordinate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class MetaTable {

    private final String tableName;
    private final ArrayList<MetaAttribute> metaAttributes;
    private final ArrayList<Coordinate> pointers;
    private final int binarySize;

    public MetaTable(String tableName,
        ArrayList<MetaAttribute> metaAttributes) {
        this.tableName = tableName;
        this.metaAttributes = metaAttributes;
        pointers = constructPointers();
        binarySize = calculateBinarySize();
    }

    public MetaTable(String tableName, ArrayList<MetaAttribute> metaAttributes,
        ArrayList<Coordinate> pointers, int binarySize) {
        this.tableName = tableName;
        this.metaAttributes = metaAttributes;
        this.pointers = pointers;
        this.binarySize = binarySize;
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
            Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE));
        index += Constant.INTEGER_SIZE;
        String name = Helper.convertByteArrayToString(
            Arrays.copyOfRange(bytes, index, index + nameLength));
        index += nameLength;

        int numOfMetaAttributes = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE));
        index += Constant.INTEGER_SIZE;

        ArrayList<MetaAttribute> metaAttributes = new ArrayList<>();
        ArrayList<Coordinate> pointers = new ArrayList<>();

        while (numOfMetaAttributes > 0) {
            Coordinate coordinate = Coordinate.deserialize(
                Arrays.copyOfRange(bytes, index, index + Coordinate.getBinarySize()));
            pointers.add(coordinate);
            index += Coordinate.getBinarySize();

            MetaAttribute metaAttribute = MetaAttribute.deserialize(
                Arrays.copyOfRange(bytes, coordinate.getOffset(), coordinate.getOffset() + coordinate.getLength()));
            metaAttributes.add(metaAttribute);
            numOfMetaAttributes--;
        }
        return new MetaTable(name, metaAttributes, pointers, bytes.length);
    }

    private int calculateBinarySize() {
        int binarySize = Constant.INTEGER_SIZE;
        binarySize += tableName.getBytes().length;
        binarySize += Constant.INTEGER_SIZE;
        binarySize += Coordinate.getBinarySize() * pointers.size();
        for (MetaAttribute metaAttribute : metaAttributes) {
            binarySize += metaAttribute.calculateBinarySize();
        }
        return binarySize;
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
            pointersBytes = Helper.concatenate(pointersBytes, pointerBytes);
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
        byte[] nameBytes = Helper.convertStringToByteArrays(tableName);
        int offset = (Constant.INTEGER_SIZE * 2) + nameBytes.length + Coordinate.getBinarySize() * metaAttributes.size();
        for (MetaAttribute metaAttribute : metaAttributes) {
            int metaAttributeBinarySize = metaAttribute.getBinarySize();
            pointers.add(new Coordinate(offset, metaAttributeBinarySize));
            offset += metaAttributeBinarySize;
        }
        return pointers;
    }

    public String tableName() {
        return tableName;
    }

    public ArrayList<MetaAttribute> metaAttributes() {
        return metaAttributes;
    }

    public int getBinarySize() {
        return binarySize;
    }

    @Override
    public String toString() {
        return "MetaTable{" +
            "tableName='" + tableName + '\'' +
            ", metaAttributes=" + metaAttributes +
            ", pointers=" + pointers +
            ", binarySize=" + binarySize +
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
        MetaTable metaTable = (MetaTable) o;
        return binarySize == metaTable.binarySize && tableName.equals(metaTable.tableName)
            && metaAttributes.equals(metaTable.metaAttributes) && pointers.equals(
            metaTable.pointers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, metaAttributes, pointers, binarySize);
    }
}
