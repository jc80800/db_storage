import Constants.Constant;
import Objects.Statement;
import Objects.StorageManager;

import java.io.File;
import java.util.Scanner;

public class Main {

    /**
     * Entry Program: java src.Main <db loc> <page size> <buffer size>
     */
    public static void main(String[] args) {
        if(args.length != 3){
            System.err.println("Argument Length invalid");
            System.exit(1);
        }

        start(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }

    /**
     * Main interface with user. Will continuously ask for command until quit
     * @param dbLoc
     * @param pageSize
     * @param bufferSize
     */
    public static void start(String dbLoc, int pageSize, int bufferSize) {

        // Check if Directory exist, if not create else restart
        File db = new File(dbLoc);

        if(!checkDirectory(db)){
            System.exit(1);
        }

        // Create Storage Manager
        StorageManager storageManager = new StorageManager();

        while(true) {
            // while running command
            Scanner scanner = new Scanner(System.in);
            System.out.print(Constant.PROMPT);

            String command = scanner.nextLine();

            Statement statement = new Statement();
            Constant.PrepareResult prepareResult = statement.prepareStatement(command);
            switch (prepareResult) {
                case PREPARE_QUIT -> {
                    System.out.println("Closing database");
                }
                case PREPARE_SUCCESS -> {
                }
                case PREPARE_UNRECOGNIZED_STATEMENT -> {
                    System.out.printf("Unrecognized keyword at \"%s\".\n", command);
                    continue;
                }
            }
            statement.execute();
        }
    }

    /**
     * Helper function for checking directory
     * If directory exist or created, return file
     * else system exist
     */
    private static Boolean checkDirectory(File f){

        if(f.exists() && f.isDirectory()) {
            System.out.println("Directory" + f.getName() + " exists");
            return true;
        } else {
            // Directory needs to be in the format of ./foo
            if(f.mkdirs()) {
                System.out.println("Directory " + f.getName() + " has been created");
                return true;
            } else {
                System.err.println("Directory could not be created");
            }
        }
        return false;
    }
}
