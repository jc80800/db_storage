package StorageManager;

import StorageManager.Metadata.Attribute.MetaAttribute;
import StorageManager.Metadata.Catalog;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class StorageManager {

    private final File db;
    private ArrayList<RandomAccessFile> tables;
    private Catalog catalog;

    public StorageManager(File db) {
        this.db = db;
        /**
        for (File file : Objects.requireNonNull(db.listFiles())) {
            try {
                tables.add(new RandomAccessFile(file, "rw"));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }**/
    }

    public void createTable(String name, ArrayList<MetaAttribute> attributes){
        System.out.println(name);
        for(MetaAttribute a : attributes){
            System.out.println(a.getName());
            System.out.println(a.getType());
        }
    }

    public void executeSelect(String table){
        // Check if the file exist in the directory

        File table_file = getTableFile(table);
        if(table_file.exists()){
            // TODO determine what is the process after this? We want to read the entire file
        } else {
            System.out.println("File does not exist");
        }
    }

    public void executeInsert(String table, String[] values) {
        File table_file = getTableFile(table);
        if(!table_file.exists()){
            createNewFile(table_file);
        } else {
            searchForRecordPlacement(table_file);
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

    public void displaySchema() {
        // database location
        // page size
        // buffer size
        // table schema

    }

    public void searchForRecordPlacement(File file){
        // TODO loop through and search for the appropriate spot and insert record
    }

    public void createNewFile(File file) {
        try {
            if(file.createNewFile()){
                System.out.println("File for " + file.getName() + " has been created");

                // TODO make new page and add to file

            } else {
                System.out.println("Unable to create new File for " + file.getName());
            }
        } catch (IOException e) {
            System.out.println("Unable to create new File for " + file.getName());
        }
    }

    public File getTableFile(String table){
        String table_path = db.getName() + "/" + table;
        return new File(table_path);
    }
}
