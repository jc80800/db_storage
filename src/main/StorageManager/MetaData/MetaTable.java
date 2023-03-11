package main.StorageManager.MetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import main.Constants.Constant;
import main.Constants.Coordinate;
import main.Constants.Helper;

public final class MetaTable {

    private final int tableNumber;
    private String tableName;
    private final ArrayList<MetaAttribute> metaAttributes;
    private ArrayList<Coordinate> pointers;
    private int binarySize;

    public MetaTable(int tableNumber, String tableName,
        ArrayList<MetaAttribute> metaAttributes) {
        this.tableNumber = tableNumber;
        this.tableName = tableName;
        this.metaAttributes = metaAttributes;
        pointers = constructPointers();
        binarySize = calculateBinarySize();
    }

    public MetaTable(int tableNumber, String tableName, ArrayList<MetaAttribute> metaAttributes,
        ArrayList<Coordinate> pointers, int binarySize) {
        this.tableNumber = tableNumber;
        this.tableName = tableName;
        this.metaAttributes = metaAttributes;
        this.pointers = pointers;
        this.binarySize = binarySize;
    }

    /**
     * form: [tableNumber(int), nameLength(int), name(String), #ofAttributes(int), list of
     * coordinates(coordinate), list of attributes(metaAttribute)]
     *
     * @return MetaTable
     */
    public static MetaTable deserialize(byte[] bytes) {
        int index = 0;
        int tableNumber = Helper.convertByteArrayToInt(
            Arrays.copyOf(bytes, index += Constant.INTEGER_SIZE));
        int nameLength = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index += Constant.INTEGER_SIZE));
        System.out.println(nameLength);
        String name = Helper.convertByteArrayToString(
            Arrays.copyOfRange(bytes, index, index += nameLength));

        System.out.println(name);
        int numOfMetaAttributes = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index += Constant.INTEGER_SIZE));

        ArrayList<MetaAttribute> metaAttributes = new ArrayList<>();
        ArrayList<Coordinate> pointers = new ArrayList<>();

        while (numOfMetaAttributes > 0) {
            Coordinate coordinate = Coordinate.deserialize(
                Arrays.copyOfRange(bytes, index, index += Coordinate.getBinarySize()));
            pointers.add(coordinate);

            System.out.println("Problem arised here");
            MetaAttribute metaAttribute = MetaAttribute.deserialize(
                Arrays.copyOfRange(bytes, coordinate.getOffset(),
                    coordinate.getOffset() + coordinate.getLength()));
            metaAttributes.add(metaAttribute);
            numOfMetaAttributes--;
        }
        return new MetaTable(tableNumber, name, metaAttributes, pointers, bytes.length);
    }

    private int calculateBinarySize() {
        int binarySize = Constant.INTEGER_SIZE;
        binarySize += tableName.getBytes().length;
        binarySize += Constant.INTEGER_SIZE;
        binarySize += Constant.INTEGER_SIZE;
        binarySize += Coordinate.getBinarySize() * pointers.size();
        for (MetaAttribute metaAttribute : metaAttributes) {
            binarySize += metaAttribute.calculateBinarySize();
        }
        return binarySize;
    }

    public void changeName(String name){
        this.tableName = name;
        this.pointers = constructPointers();
        this.binarySize = calculateBinarySize();
    }

    /**
     * form: [tableNumber(int), nameLength(int), name(String), #ofAttributes(int), list of
     * coordinates(Coordinate), list of attributes(metaAttribute)]
     *
     * @return byte array
     */
    public byte[] serialize() {
        System.out.println(tableNumber);
        System.out.println(tableName);
        System.out.println(tableName.length());
        System.out.println(metaAttributes.size());

        byte[] tableNumberBytes = Helper.convertIntToByteArray(tableNumber);
        byte[] nameLengthBytes = Helper.convertIntToByteArray(tableName.length());
        byte[] nameBytes = Helper.convertStringToByteArrays(tableName);
        byte[] numOfMetaAttributes = Helper.convertIntToByteArray(metaAttributes.size());

        byte[] pointersBytes = new byte[0];
        for (Coordinate pointer : pointers) {
            System.out.println(pointer);
            byte[] pointerBytes = pointer.serialize();
            pointersBytes = Helper.concatenate(pointersBytes, pointerBytes);
        }

        byte[] metaAttributesBytes = new byte[0];
        for (MetaAttribute metaAttribute : metaAttributes) {
            byte[] metaAttributeBytes = metaAttribute.serialize();
            metaAttributesBytes = Helper.concatenate(metaAttributesBytes, metaAttributeBytes);
        }

        return Helper.concatenate(tableNumberBytes, nameLengthBytes, nameBytes, numOfMetaAttributes,
            pointersBytes, metaAttributesBytes);
    }

    private ArrayList<Coordinate> constructPointers() {
        ArrayList<Coordinate> pointers = new ArrayList<>();
        byte[] nameBytes = Helper.convertStringToByteArrays(tableName);
        int offset = (Constant.INTEGER_SIZE * 3) + nameBytes.length
            + Coordinate.getBinarySize() * metaAttributes.size();
        for (MetaAttribute metaAttribute : metaAttributes) {
            int metaAttributeBinarySize = metaAttribute.getBinarySize();
            pointers.add(new Coordinate(offset, metaAttributeBinarySize));
            offset += metaAttributeBinarySize;
        }
        return pointers;
    }

    public String getTableName() {
        return tableName;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public ArrayList<MetaAttribute> metaAttributes() {
        return metaAttributes;
    }

    public int getBinarySize() {
        return binarySize;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Table name: ").append(tableName).append("\n");
        sb.append("Table schema: \n");
        for (MetaAttribute metaAttribute : metaAttributes) {
            sb.append("\t").append(metaAttribute.toString());
        }
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
        MetaTable metaTable = (MetaTable) o;
        return tableNumber == metaTable.tableNumber && binarySize == metaTable.binarySize
            && tableName.equals(metaTable.tableName) && metaAttributes.equals(
            metaTable.metaAttributes)
            && pointers.equals(metaTable.pointers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableNumber, tableName, metaAttributes, pointers, binarySize);
    }
}
