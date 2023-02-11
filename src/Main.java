import Constants.Constant;
import SqlParser.Statement;
import StorageManager.Metadata.Catalog;
import StorageManager.StorageManager;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class Main {


    /**
     * Entry Program: java src.Main <db loc> <page size> <buffer size>
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Argument Length invalid");
            System.exit(1);
        }

        start(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }

    /**
     * Main interface with user. Will continuously ask for command until quit
     *
     * @param dbLoc
     * @param pageSize
     * @param bufferSize
     */
    public static void start(String dbLoc, int pageSize, int bufferSize) {

        // Check if Directory exist, if not create else restart
        File db = new File(dbLoc);

        // Create Storage Manager
        StorageManager storageManager = new StorageManager(db, pageSize, bufferSize);

        if (!checkDirectory(db, storageManager)) {
            System.exit(1);
        }

        Scanner scanner = new Scanner(System.in);

        while (true) {
            // while running command
            System.out.print(Constant.PROMPT);

            String command = scanner.nextLine();

            Statement statement = new Statement(storageManager);
            Constant.PrepareResult prepareResult = statement.prepareStatement(command);
            switch (prepareResult) {
                case PREPARE_QUIT -> {
                    System.out.println("Closing database");
                    scanner.close();
                    System.exit(0);
                }
                case PREPARE_SUCCESS -> {
                    System.out.println("Execution Completed");
                }
                case PREPARE_UNRECOGNIZED_STATEMENT -> System.out.printf("Unrecognized keyword at \"%s\".\n", command);
            }
        }
    }

    /**
     * Helper function for checking directory If directory exist or created, return file else system
     * exist
     */
    private static Boolean checkDirectory(File f, StorageManager storageManager) {

        File catalog_file = new File(f.getPath() + "/Catalog.txt");

        if (f.exists() && f.isDirectory()) {
            System.out.println("Directory " + f.getName() + " exists");
            // parse the catalog file
            // set the catalog in the storage manager
            storageManager.parseCatalog(catalog_file);
            return true;
        } else {
            // Directory needs to be in the format of ./foo
            if (f.mkdirs()) {
                System.out.println("Directory " + f.getName() + " has been created");
                try {
                    if (catalog_file.createNewFile()){
                        Catalog catalog = storageManager.createNewCatalog();
                        RandomAccessFile randomAccessFile = new RandomAccessFile(catalog_file, "rw");
                        randomAccessFile.write(catalog.serialize());
                        randomAccessFile.close();
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                System.err.println("Directory could not be created");
            }
        }
        return false;
    }
}
