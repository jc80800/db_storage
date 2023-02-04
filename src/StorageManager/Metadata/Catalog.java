package StorageManager.Metadata;

import java.util.HashMap;

public class Catalog {

    private int numOfTables;
    private HashMap<Integer, Metatable> metaTable;

    public Catalog(int numOfTables, HashMap<Integer, Metatable> metaTable) {
        this.numOfTables = numOfTables;
        this.metaTable = metaTable;
    }

    public int getNumOfTables() {
        return numOfTables;
    }

    public void setNumOfTables(int numOfTables) {
        this.numOfTables = numOfTables;
    }

    public HashMap<Integer, Metatable> getMetaTable() {
        return metaTable;
    }

    public void setMetaTable(HashMap<Integer, Metatable> metaTable) {
        this.metaTable = metaTable;
    }
}
