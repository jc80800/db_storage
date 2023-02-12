package StorageManager.Metadata;

import Constants.Constant;
import Constants.Helper;
import StorageManager.Coordinate;
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

    public static Catalog deserialize(byte[] bytes) {
        int index = 0;
        int pageSize = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, Constant.INTEGER_SIZE + 1));
        index += Constant.INTEGER_SIZE + 1;
        int numOfMetaTables = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, Constant.INTEGER_SIZE + 1));
        index += Constant.INTEGER_SIZE + 1;

        ArrayList<Coordinate> pointers = new ArrayList<>();
        HashMap<Integer, MetaTable> metaTableHashMap = new HashMap<>();
        int tableNum = 1;
        while (numOfMetaTables > 0) {
            Coordinate coordinate = Coordinate.deserialize(
                Arrays.copyOfRange(bytes, index, index + Coordinate.getBinarySize() + 1));
            pointers.add(coordinate);

            MetaTable metaTable = MetaTable.deserialize(
                Arrays.copyOfRange(bytes, coordinate.getOffset(), coordinate.getLength()));
            metaTableHashMap.put(tableNum++, metaTable);
            numOfMetaTables--;
        }
        return new Catalog(pageSize, pointers, metaTableHashMap);
    }

    private ArrayList<Coordinate> constructPointers() {
        ArrayList<Coordinate> pointers = new ArrayList<>();
        int offset = Constant.INTEGER_SIZE * 2;
        for (MetaTable metaTable : metaTableHashMap.values()) {
            int metaTableBinarySize = metaTable.getBinarySize();
            pointers.add(new Coordinate(offset, metaTableBinarySize));
            offset += metaTableBinarySize + 1;
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

        byte[] pointersBytes = new byte[0];
        for (Coordinate pointer : pointers) {
            byte[] pointerBytes = pointer.serialize();
            Helper.concatenate(pointersBytes, pointerBytes);
        }

        byte[] metaTablesBytes = new byte[0];
        for (MetaTable metaTable : metaTableHashMap.values()) {
            byte[] metaTableBytes = metaTable.serialize();
            Helper.concatenate(metaTablesBytes, metaTableBytes);
        }
        return Helper.concatenate(pageSizeBytes, numOfMetaTables, pointersBytes, metaTablesBytes);
    }

    public HashMap<Integer, MetaTable> getMetaTableHashMap() {
        return metaTableHashMap;
    }

    public void setMetaTableHashMap(HashMap<Integer, MetaTable> metaTableHashMap) {
        this.metaTableHashMap = metaTableHashMap;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    @Override
    public String toString() {
        return "Catalog{" +
            "pageSize=" + pageSize +
            ", pointers=" + pointers +
            ", metaTableHashMap=" + metaTableHashMap +
            '}';
    }
}
