package StorageManager;

import StorageManager.Metadata.Catalog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Objects;

public class StorageManager {

    private final File db;
    private ArrayList<RandomAccessFile> tables;
    private Catalog catalog;

    public StorageManager(File db) {
        this.db = db;
        for (File file : Objects.requireNonNull(db.listFiles())) {
            try {
                tables.add(new RandomAccessFile(file, "rw"));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
