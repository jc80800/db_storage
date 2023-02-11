package StorageManager.Metadata;

import java.io.File;
import java.util.HashMap;

public class Catalog {

    private final int pageSize;
    private int numOfTables;
    private HashMap<Integer, MetaTable> metaTable;

    public Catalog(int pageSize, int numOfTables, HashMap<Integer, Metatable> metaTable) {
        this.numOfTables = numOfTables;
        this.metaTable = metaTable;
        this.pageSize = pageSize;
    }

    public int getNumOfTables() {
        return numOfTables;
    }

    public void setNumOfTables(int numOfTables) {
        this.numOfTables = numOfTables;
    }

    public HashMap<Integer, MetaTable> getMetaTable() {
        return metaTable;
    }

    public int getPageSize(){
        return this.pageSize;
    }

    public void setMetaTable(HashMap<Integer, MetaTable> metaTable) {
        this.metaTable = metaTable;
    }

    public String stringifyMetaTable(){
        return null;
    }

    public static Catalog deserialize(File file){
        return null;
    }

    public byte[] serialize(){
        return new byte[0];
    }
}
