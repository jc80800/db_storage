package StorageManager;

import StorageManager.Metadata.MetaAttribute;
import StorageManager.Metadata.Catalog;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class StorageManager {

    private final File db;
    private Catalog catalog;
    private final int pageSize;
    private final int bufferSize;

    /**
     * Phase 1: create table
     *          insert
     *          select *
     *          DisplayInfo()
     *          DisplaySchema()
     * @param db
     * @param pageSize
     * @param bufferSize
     */
    public StorageManager(File db, int pageSize, int bufferSize) {
        this.db = db;
        this.pageSize = pageSize;
        this.bufferSize = bufferSize;
    }

    public void createTable(String table_name, String[] values){
        // input = create table foo( age char(10), num integer primarykey )
        // values = [ age char(10), num integer primarykey ]
        // table_name = foo

        // TODO parse the attributes and make sure it's gucci
        // if so, create table
        // if not, print error
    }

    public void executeSelect(String table){
        // Check if the file exist in the directory

        File table_file = getTableFile(table);
        if(table_file.exists()){ // TODO change this to checking the catalog instead
            // TODO
        } else {
            System.out.println("File does not exist");
        }
    }

    public void executeInsert(String table, String[] values) {
        File table_file = getTableFile(table);
        if(table_file.exists()){
            searchForRecordPlacement(table_file);
        } else {
            System.out.println("Table doesn't exist");
        }

        /* Example of RandomAccessFile Usage
        try {
            RandomAccessFile file = new RandomAccessFile(table_file.getPath(), "rw");
            file.write("something here".getBytes(StandardCharsets.UTF_8));
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

         */

        // TODO Same as executeSelect
    }

    public void displayInfo(String table) {
        File table_file = getTableFile(table);
        if(table_file != null){
            // TODO Same as executeSelect
        }
    }

    /**
     * NEED FINESSE
     */
    public void displaySchema() {
        // database location
        // page size
        // buffer size
        // table schema
        System.out.println(db.getPath());
        System.out.println(this.pageSize);
        System.out.println(this.bufferSize);
        System.out.println(this.catalog.stringifyMetaTable());

    }

    public void searchForRecordPlacement(File file){
        // TODO loop through and search for the appropriate spot and insert record
    }

    public Catalog createNewCatalog(){
        this.catalog = new Catalog(this.pageSize, 0, new HashMap<>());
        return this.catalog;
    }

    public void parseCatalog(File catalog_file) {
        // Deserialize the file and return a catalog
        this.catalog = Catalog.deserialize(catalog_file);
    }

    public File getTableFile(String table){
        String table_path = db.getName() + "/" + table;
        return new File(table_path);
    }
}
