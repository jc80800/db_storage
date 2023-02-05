package StorageManager;

import StorageManager.Metadata.Attribute.MetaAttribute;
import StorageManager.Metadata.Catalog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Objects;

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
}
