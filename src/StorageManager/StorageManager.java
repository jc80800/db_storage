package StorageManager;

import Constants.Constant;
import Constants.Constant.DataType;
import Constants.Helper;
import StorageManager.Metadata.MetaAttribute;
import StorageManager.Metadata.Catalog;
import StorageManager.Metadata.MetaTable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

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

        ArrayList<MetaAttribute> attributes = new ArrayList<>();
        HashSet<String> seenAttributeNames = new HashSet<>();
        boolean foundPrimaryKey = false;
        for (String value : values) {
            value = value.trim();
            String[] valArray = value.split(" ");
            boolean isPrimary = false;
            if (valArray.length < 2) {
                System.out.println("ERROR: Missing fields for table attribute!");
                return;
            } else if (valArray.length > 3 || (valArray.length == 3 && !valArray[2].equalsIgnoreCase("primarykey"))) {
                System.out.println("ERROR: Too many fields found for table attribute!");
                return;
            } else {
                if (valArray.length == 3 && foundPrimaryKey) {
                    System.out.println("ERROR: Found more than one primary key!");
                    return;
                } else if (valArray.length == 3) {
                    isPrimary = true;
                    foundPrimaryKey = true;
                }

                if (seenAttributeNames.contains(valArray[0].toLowerCase())) {
                    System.out.println("ERROR: One or more attributes have the same name!");
                    return;
                } else {
                    seenAttributeNames.add(valArray[0].toLowerCase());
                }
                if (valArray[1].matches("(?i)INTEGER|DOUBLE|BOOLEAN")) {
                    Constant.DataType type = Constant.DataType.INTEGER;
                    if (valArray[1].equalsIgnoreCase("DOUBLE")) {
                        type = Constant.DataType.DOUBLE;
                    } else if (valArray[1].equalsIgnoreCase("BOOLEAN")) {
                        type = Constant.DataType.BOOLEAN;
                    }
                    attributes.add(new MetaAttribute(isPrimary, valArray[0].toLowerCase(), type));
                } else if (valArray[1].matches("(?i)CHAR\\([0-9]+\\)|VARCHAR\\([0-9]+\\)")) {
                    String[] typeArray = valArray[1].split("\\(");
                    Constant.DataType type = Constant.DataType.CHAR;
                    if (typeArray[0].equalsIgnoreCase("VARCHAR")) {
                        type = Constant.DataType.VARCHAR;
                    }
                    int length = Integer.parseInt(typeArray[1].replace(")", ""));
                    attributes.add(new MetaAttribute(isPrimary, valArray[0].toLowerCase(), type, length));
                } else {
                    System.out.println("ERROR: Invalid attribute type was found!");
                    return;
                }
            }
        }

        if (!foundPrimaryKey) {
            System.out.println("ERROR: No primary key was specified!");
            return;
        }

        MetaTable table = new MetaTable(table_name, attributes);
        this.catalog.putMetaTable(table);

        System.out.println("Table created");
        System.out.println(table);

        updateCatalog();
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
            return;
        }

        int pageSize = catalog.getPageSize();
        try{
            RandomAccessFile randomAccessFile = new RandomAccessFile(table_file.getPath(), "rw");
            randomAccessFile.seek(4);

            //get number of pages in table
            byte[] numPagesBytes = new byte[4];
            randomAccessFile.read(numPagesBytes);
            int numPages = Helper.convertByteArrayToInt(numPagesBytes);

            // Get coordinate object of last page
            randomAccessFile.seek(12 + (numPages - 1) * 8L);
            byte[] coordinateBytes = new byte[8];
            randomAccessFile.read(coordinateBytes);
            Coordinate coordinate = Coordinate.deserialize(coordinateBytes);

            // Check capacity of page
            randomAccessFile.seek(coordinate.getOffset());
            byte[] pageBytes = new byte[coordinate.getLength()];
            randomAccessFile.read(pageBytes);
            Page currentPage = Page.deserialize(pageBytes);

            //if page can fit another record
            //if(pageSize - coordinate.getLength() >= )
                // if so, add to page, write new page object to file, update coordinate/pointers
            //else, make new page object, make and add new coordinate object/ update pointers, write to file


            randomAccessFile.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        /* Proposed steps for inserting
        1. Get page size from catalog object
        2. Traverse to last page of the table file
        3. Add record if page is not full. Else, split page in half and insert new record
        4. Update table pointers for new page
         */
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

    /**
     * TODO need to test if it works after populating with create table and such
     * @param table
     */
    public void displayInfo(String table) {
        File table_file = getTableFile(table);
        if(table_file != null){
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(table_file.getPath(), "rw");

                // Access file for information
                randomAccessFile.seek(0);
                int tableNumber = randomAccessFile.readInt();
                int numOfPages = randomAccessFile.readInt();
                int numOfRecords = randomAccessFile.readInt();
                MetaTable metaTable = this.catalog.getMetaTable(tableNumber);
                System.out.println(metaTable);
                System.out.println(tableNumber);
                System.out.println(numOfPages);
                System.out.println(numOfRecords);
                randomAccessFile.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * loop through each table schema in catalog and print
     */
    public void displaySchema() {
        // database location
        // page size
        // buffer size
        // table schema
        System.out.println(db.getPath());
        System.out.println(this.pageSize);
        System.out.println(this.bufferSize);
        System.out.println(this.catalog.toString());

    }

    public void searchForRecordPlacement(File file){
        // TODO loop through and search for the appropriate spot and insert record

    }

    public void createNewCatalog(){
        this.catalog = new Catalog(this.pageSize);
    }

    public void updateCatalog(){
        File catalog_file = new File(this.db + "/" + "Catalog");

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(catalog_file,
                "rw");
            byte[] bytes = this.catalog.serialize();
            randomAccessFile.write(bytes);
            randomAccessFile.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseCatalog(File catalog_file) {
        // Deserialize the file and return a catalog

        try (RandomAccessFile raf = new RandomAccessFile(catalog_file, "rw")) {
            int fileLength = (int) raf.length();
            byte[] bytes = new byte[fileLength];
            raf.readFully(bytes);
            this.catalog = Catalog.deserialize(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getTableFile(String table){
        String table_path = db.getName() + "/" + table;
        return new File(table_path);
    }
}
