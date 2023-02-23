package main;

import main.Constants.Constant;
import main.SqlParser.SqlParser;
import main.StorageManager.StorageManager;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {


    /**
     * Entry Program: java src.main.Main <db loc> <page size> <buffer size>
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Argument Length invalid");
            System.exit(1);
        }

        start(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }

    /**
     * main.Main interface with user. Will continuously ask for command until quit
     *
     * @param dbLoc
     * @param pageSize
     * @param bufferSize
     */
    public static void start(String dbLoc, int pageSize, int bufferSize) {

        // Check if Directory exist, if not create else restart
        File db = new File(dbLoc);

        if (!checkDirectory(db)) {
            System.exit(1);
        }

        // Create Storage Manager
        StorageManager storageManager = new StorageManager(db, pageSize, bufferSize);
        checkCatalog(db, storageManager);

        SqlParser sqlParser = new SqlParser(storageManager);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // while running command
            System.out.print(Constant.PROMPT);

            String command = scanner.nextLine();

            Constant.PrepareResult prepareResult = sqlParser.prepareStatement(command);
            switch (prepareResult) {
                case PREPARE_QUIT -> {
                    System.out.println("Closing database");
                    storageManager.saveData();
                    scanner.close();
                    System.exit(0);
                }
                case PREPARE_SUCCESS -> {
                    System.out.println("SUCCESS");
                }
                case PREPARE_UNRECOGNIZED_STATEMENT -> {
                    System.out.println("ERROR");
                }
            }
        }
    }


    private static void checkCatalog(File f, StorageManager storageManager){
        File catalog_file = new File(f.getPath() + Constant.CATALOG_FILE);

        if(catalog_file.exists()){
            System.out.println("Catalog Exist, parsing");
            storageManager.parseCatalog(catalog_file);
        } else {
            try {
                System.out.println("Catalog doesn't exist, Creating");
                if (catalog_file.createNewFile()) {
                    storageManager.createNewCatalog();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Helper function for checking directory If directory exist or created, return file else system
     * exist
     */
    private static Boolean checkDirectory(File f) {

        if (f.exists() && f.isDirectory()) {
            System.out.println("Directory " + f.getName() + " exists");
            return true;
        } else {
            // Directory needs to be in the format of ./foo
            if (f.mkdirs()) {
                System.out.println("Directory " + f.getName() + " has been created");
                return true;
            } else {
                System.out.println("Directory could not be created");
            }
        }
        return false;
    }
}
