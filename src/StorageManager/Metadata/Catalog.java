package StorageManager.Metadata;

import java.util.HashMap;

public class Catalog {

    private final int pageSize;
    private int numOfTables;
    private HashMap<Integer, MetaTable> metaTable;

    public Catalog(int pageSize, int numOfTables, HashMap<Integer, MetaTable> metaTable) {
        this.pageSize = pageSize;
        this.numOfTables = numOfTables;
        this.metaTable = metaTable;
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

    public void setMetaTable(HashMap<Integer, MetaTable> metaTable) {
        this.metaTable = metaTable;
    }
}
