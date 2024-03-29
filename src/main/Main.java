package main;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import main.Constants.Constant;
import main.SqlParser.SqlParser;
import main.StorageManager.StorageManager;

public class Main {

    /**
     * Entry Program: java src.main.Main <db loc> <page size> <buffer size>
     */
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Argument Length invalid");
            System.exit(1);
        }

        boolean index = args[3].equals("true");
        start(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), index);
    }

    /**
     * main.Main interface with user. Will continuously ask for command until quit
     *
     */
    public static void start(String dbLoc, int pageSize, int bufferSize, boolean index) {
        // Check if Directory exist, if not create else restart
        File db = new File(dbLoc);

        // Create Storage Manager
        StorageManager storageManager = new StorageManager(db, pageSize, bufferSize, index);
        storageManager.initializeDB();

        SqlParser sqlParser = new SqlParser(storageManager);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // while running command
            System.out.print(Constant.PROMPT);

            StringBuilder command = new StringBuilder();

            String line = scanner.nextLine();
            while(!line.contains(";") && !line.equals("<quit>")){
                command.append(line);
                line = scanner.nextLine();
            }

            command.append(line);

            Constant.PrepareResult prepareResult = sqlParser.prepareStatement(command.toString());

            switch (prepareResult) {
                case PREPARE_QUIT -> {
                    System.out.println("\nSafely shutting down the database...");
                    scanner.close();
                    storageManager.saveData();
                    System.out.println("\nExiting the database...");
                    System.exit(0);
                }
                case PREPARE_SUCCESS -> System.out.println("SUCCESS\n");
                case PREPARE_UNRECOGNIZED_STATEMENT -> System.out.println("ERROR\n");
            }
        }
    }
}