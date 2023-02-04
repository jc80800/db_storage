package StorageManager;

import StorageManager.Metadata.Metatable;
import java.util.HashMap;

public class Catalog {

    private int numOfTables;
    private HashMap<Integer, Metatable> tablesMetadata;

    public Catalog(int numOfTables, HashMap<Integer, Metatable> tablesMetadata) {
        this.numOfTables = numOfTables;
        this.tablesMetadata = tablesMetadata;
    }

    public int getNumOfTables() {
        return numOfTables;
    }

    public void setNumOfTables(int numOfTables) {
        this.numOfTables = numOfTables;
    }

    public HashMap<Integer, Metatable> getTablesMetadata() {
        return tablesMetadata;
    }

    public void setTablesMetadata(HashMap<Integer, Metatable> tablesMetadata) {
        this.tablesMetadata = tablesMetadata;
    }
}
