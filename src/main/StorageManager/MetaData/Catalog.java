package main.StorageManager.MetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import main.Constants.Constant;
import main.Constants.Coordinate;
import main.Constants.Helper;

public class Catalog {

    private final int pageSize;
    private int nextTableNumber;
    private ArrayList<Coordinate> pointers;
    private HashMap<Integer, MetaTable> metaTableHashMap;

    public Catalog(int pageSize) {
        this.pageSize = pageSize;
        this.metaTableHashMap = new HashMap<>();
        this.pointers = new ArrayList<>();
        this.nextTableNumber = 0;
    }

    public Catalog(int pageSize, HashMap<Integer, MetaTable> metaTableHashMap) {
        this.pageSize = pageSize;
        this.metaTableHashMap = metaTableHashMap;
        this.pointers = constructPointers();
    }

    public Catalog(int pageSize, ArrayList<Coordinate> pointers,
        HashMap<Integer, MetaTable> metaTableHashMap, int nextTableNumber) {
        this.pageSize = pageSize;
        this.pointers = pointers;
        this.metaTableHashMap = metaTableHashMap;
        this.nextTableNumber = nextTableNumber;
    }

    public static Catalog deserialize(byte[] bytes) {
        int index = 0;
        int pageSize = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index += Constant.INTEGER_SIZE));
        int numOfMetaTables = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index += Constant.INTEGER_SIZE));
        int nextTableNumber = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index += Constant.INTEGER_SIZE));

        ArrayList<Coordinate> pointers = new ArrayList<>();
        HashMap<Integer, MetaTable> metaTableHashMap = new HashMap<>();
        while (numOfMetaTables > 0) {
            Coordinate coordinate = Coordinate.deserialize(
                Arrays.copyOfRange(bytes, index, index + Coordinate.getBinarySize()));
            pointers.add(coordinate);
            index += Coordinate.getBinarySize();

            MetaTable metaTable = MetaTable.deserialize(
                Arrays.copyOfRange(bytes, coordinate.getOffset(),
                    coordinate.getOffset() + coordinate.getLength()));
            metaTableHashMap.put(metaTable.getTableNumber(), metaTable);
            numOfMetaTables--;
        }
        return new Catalog(pageSize, pointers, metaTableHashMap, nextTableNumber);
    }

    public MetaTable getMetaTable(int file_number) {
        return this.metaTableHashMap.get(file_number);
    }

    public int getNextTableNumber(){
        return this.nextTableNumber;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public int getTableSize() {
        return this.pointers.size();
    }

    public void addMetaTable(String table_name, ArrayList<MetaAttribute> attributes) {
        MetaTable metaTable = new MetaTable(nextTableNumber, table_name, attributes);
        metaTableHashMap.put(metaTable.getTableNumber(), metaTable);
        this.nextTableNumber++;
        this.pointers = constructPointers();
    }

    /**
     * Remove MetaTable by table number
     * @param tableNumber table number for table to be dropped
     */
    public void deleteMetaTable(int tableNumber) {
        metaTableHashMap.remove(tableNumber);
        pointers = constructPointers();
    }

    private ArrayList<Coordinate> constructPointers() {
        ArrayList<Coordinate> pointers = new ArrayList<>();
        int offset =
            Constant.INTEGER_SIZE * 3 + Coordinate.getBinarySize() * metaTableHashMap.size();
        for (MetaTable metaTable : metaTableHashMap.values()) {
            int metaTableBinarySize = metaTable.getBinarySize();
            pointers.add(new Coordinate(offset, metaTableBinarySize));
            offset += metaTableBinarySize;
        }
        return pointers;
    }

    /**
     * form: [pageSize(int), #ofMetatable(int), nextTableNumber(int), list of
     * coordinates(Coordinate), list of MetaTable)
     *
     * @return byte arrays
     */
    public byte[] serialize() {
        byte[] pageSizeBytes = Helper.convertIntToByteArray(pageSize);
        byte[] numOfMetaTables = Helper.convertIntToByteArray(metaTableHashMap.size());
        byte[] nextTableNumberBytes = Helper.convertIntToByteArray(nextTableNumber);
        if (metaTableHashMap.size() == 0) {
            return Helper.concatenate(pageSizeBytes, numOfMetaTables, nextTableNumberBytes);
        }

        byte[] pointersBytes = new byte[0];

        for (Coordinate pointer : pointers) {
            System.out.println(pointer);
            byte[] pointerBytes = pointer.serialize();
            pointersBytes = Helper.concatenate(pointersBytes, pointerBytes);
        }

        byte[] metaTablesBytes = new byte[0];
        for (MetaTable metaTable : metaTableHashMap.values()) {
            byte[] metaTableBytes = metaTable.serialize();
            metaTablesBytes = Helper.concatenate(metaTablesBytes, metaTableBytes);
        }

        return Helper.concatenate(pageSizeBytes, numOfMetaTables, nextTableNumberBytes,
            pointersBytes, metaTablesBytes);
    }

    public HashMap<Integer, MetaTable> getMetaTableHashMap() {
        return metaTableHashMap;
    }

    @Override
    public String toString() {
        return "Catalog{" +
            "pageSize=" + pageSize +
            ", pointers=" + pointers +
            ", metaTableHashMap=" + metaTableHashMap +
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
        Catalog catalog = (Catalog) o;
        return pageSize == catalog.pageSize && nextTableNumber == catalog.nextTableNumber
            && pointers.equals(catalog.pointers) && metaTableHashMap.equals(
            catalog.metaTableHashMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageSize, nextTableNumber, pointers, metaTableHashMap);
    }
}
