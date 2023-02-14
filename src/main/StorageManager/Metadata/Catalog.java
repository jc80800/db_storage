package main.StorageManager.Metadata;

import java.util.Objects;
import main.Constants.Constant;
import main.Constants.Helper;
import main.Constants.Coordinate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Catalog {

    private final int pageSize;
    private ArrayList<Coordinate> pointers;
    private HashMap<Integer, MetaTable> metaTableHashMap;

    public Catalog(int pageSize) {
        this.pageSize = pageSize;
        this.metaTableHashMap = new HashMap<>();
        this.pointers = new ArrayList<>();
    }

    public Catalog(int pageSize, HashMap<Integer, MetaTable> metaTableHashMap) {
        this.pageSize = pageSize;
        this.metaTableHashMap = metaTableHashMap;
        this.pointers = constructPointers();
    }

    public Catalog(int pageSize, ArrayList<Coordinate> pointers,
        HashMap<Integer, MetaTable> metaTableHashMap) {
        this.pageSize = pageSize;
        this.pointers = pointers;
        this.metaTableHashMap = metaTableHashMap;
    }

    public MetaTable getMetaTable(int file_number) {
        return this.metaTableHashMap.get(file_number);
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public int getTableSize(){
        return this.pointers.size();
    }

    public void putMetaTable(MetaTable metaTable){
        this.metaTableHashMap.put(getTableSize() + 1, metaTable);
        this.pointers = constructPointers();
    }


    public static Catalog deserialize(byte[] bytes) {
        int index = 0;
        int pageSize = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE));
        index += Constant.INTEGER_SIZE;
        int numOfMetaTables = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE));
        index += Constant.INTEGER_SIZE;

        ArrayList<Coordinate> pointers = new ArrayList<>();
        HashMap<Integer, MetaTable> metaTableHashMap = new HashMap<>();
        int tableNum = 1;
        while (numOfMetaTables > 0) {
            Coordinate coordinate = Coordinate.deserialize(
                Arrays.copyOfRange(bytes, index, index + Coordinate.getBinarySize()));
            pointers.add(coordinate);
            index += Coordinate.getBinarySize();

            MetaTable metaTable = MetaTable.deserialize(
                Arrays.copyOfRange(bytes, coordinate.getOffset(), coordinate.getOffset() + coordinate.getLength()));
            metaTableHashMap.put(tableNum++, metaTable);
            numOfMetaTables--;
        }
        return new Catalog(pageSize, pointers, metaTableHashMap);
    }

    private ArrayList<Coordinate> constructPointers() {
        ArrayList<Coordinate> pointers = new ArrayList<>();
        int offset = Constant.INTEGER_SIZE * 2 + Coordinate.getBinarySize() * metaTableHashMap.size();
        for (MetaTable metaTable : metaTableHashMap.values()) {
            int metaTableBinarySize = metaTable.getBinarySize();
            pointers.add(new Coordinate(offset, metaTableBinarySize));
            offset += metaTableBinarySize;
        }
        return pointers;
    }

    /**
     * form: [pageSize(int), #ofMetatable(int), list of coordinates(Coordinate), list of MetaTable)
     *
     * @return byte arrays
     */
    public byte[] serialize() {
        byte[] pageSizeBytes = Helper.convertIntToByteArray(pageSize);
        byte[] numOfMetaTables = Helper.convertIntToByteArray(metaTableHashMap.size());
        if (metaTableHashMap.size() == 0) {
            return Helper.concatenate(pageSizeBytes, numOfMetaTables);
        }

        byte[] pointersBytes = new byte[0];

        for (Coordinate pointer : pointers) {
            byte[] pointerBytes = pointer.serialize();
            pointersBytes = Helper.concatenate(pointersBytes, pointerBytes);
        }

        byte[] metaTablesBytes = new byte[0];
        for (int i = 1; i < pointers.size() + 1; i++){
            byte[] metaTableBytes = metaTableHashMap.get(i).serialize();
            metaTablesBytes = Helper.concatenate(metaTablesBytes, metaTableBytes);
        }

        return Helper.concatenate(pageSizeBytes, numOfMetaTables, pointersBytes, metaTablesBytes);
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
        return pageSize == catalog.pageSize && pointers.equals(catalog.pointers)
            && metaTableHashMap.equals(catalog.metaTableHashMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageSize, pointers, metaTableHashMap);
    }
}
