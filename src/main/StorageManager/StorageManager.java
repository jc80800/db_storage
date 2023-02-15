package main.StorageManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import main.Constants.Constant;
import main.Constants.Coordinate;
import main.Constants.Helper;
import main.StorageManager.Data.Page;
import main.StorageManager.Data.Record;
import main.StorageManager.Data.TableHeader;
import main.StorageManager.MetaData.Catalog;
import main.StorageManager.MetaData.MetaAttribute;
import main.StorageManager.MetaData.MetaTable;

public class StorageManager {

    private final File db;
    private final int pageSize;
    private final int bufferSize;
    private Catalog catalog;
    private PageBuffer pageBuffer;

    /**
     * Phase 1: create table insert select * DisplayInfo() DisplaySchema()
     *
     * @param db
     * @param pageSize
     * @param bufferSize
     */
    public StorageManager(File db, int pageSize, int bufferSize) {
        this.pageBuffer = new PageBuffer(bufferSize, pageSize);
        this.db = db;
        this.pageSize = pageSize;
        this.bufferSize = bufferSize;
    }

    public void createTable(String table_name, String[] values) {
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
            } else if (valArray.length > 3 || (valArray.length == 3
                && !valArray[2].equalsIgnoreCase("primarykey"))) {
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
                    attributes.add(
                        new MetaAttribute(isPrimary, valArray[0].toLowerCase(), type, length));
                } else {
                    System.out.println("ERROR: Invalid attribute type was found!");
                    System.out.println(valArray[1]);
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

    public void executeSelect(String table) {
        // Check if the file exist in the directory

        File table_file = getTableFile(table);
        if (table_file.exists()) {
            TableHeader tableHeader = TableHeader.parseTableHeader(table_file);

        } else {
            System.out.println("Table doesn't exist");
        }
    }

    public void executeInsert(String table, String[] values) {
        File table_file = getTableFile(table);
        if (!table_file.exists()) {
            System.out.println("Table doesn't exist");
            return;
        }
        TableHeader tableHeader = TableHeader.parseTableHeader(table_file);
        if (tableHeader == null) {
            System.out.println("Header couldn't be parsed");
            return;
        }

        int tableNumber = tableHeader.getTableNumber();
        MetaTable metaTable = this.catalog.getMetaTable(tableNumber);

        ArrayList<Record> records = Record.parseRecords(values, metaTable);

        findRecordPlacement(table_file, tableHeader, records, metaTable);

    }

    /**
     * TODO need to test if it works after populating with create table and such
     *
     * @param table
     */
    public void displayInfo(String table) {
        boolean foundTable = false;
        for (int i = 1; i <= catalog.getTableSize(); i++) {
            MetaTable metaTable = catalog.getMetaTable(i);
            if (metaTable.tableName().equals(table)) {
                foundTable = true;
                System.out.println(metaTable);
                break;
            }
        }
        if (!foundTable) {
            System.out.format("No such table %s\n", table);
            return;
        }
        File table_file = getTableFile(table);
        int numOfPages = 0;
        int numOfRecords = 0;
        if (table_file.exists()) {
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(table_file.getPath(),
                    "rw");

                // Access file for information
                randomAccessFile.seek(0);
                int tableNumber = randomAccessFile.readInt();
                numOfPages = randomAccessFile.readInt();
                numOfRecords = randomAccessFile.readInt();
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.format("Pages: %d\n", numOfPages);
        System.out.format("Records: %d\n", numOfRecords);
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
        int numOfTables = catalog.getTableSize();
        if (numOfTables == 0) {
            System.out.println("No tables to display");
            return;
        }
        System.out.println("Tables:");
        for (int i = 1; i <= numOfTables; i++) {
            MetaTable metaTable = catalog.getMetaTable(i);
            System.out.println(metaTable.toString());
            File table_file = getTableFile(metaTable.tableName());
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(table_file.getPath(),
                    "rw");
                byte[] bytes = new byte[8];
                randomAccessFile.readFully(bytes, Constant.INTEGER_SIZE, Constant.INTEGER_SIZE * 2);
                int numOfPages = Helper.convertByteArrayToInt(Arrays.copyOf(bytes, 4));
                int numOfRecords = Helper.convertByteArrayToInt(Arrays.copyOfRange(bytes, 4, 8));
                System.out.format("Pages: %s\n", numOfPages);
                System.out.format("Records: %s\n", numOfRecords);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void findRecordPlacement(File table_file, TableHeader tableHeader, ArrayList<Record> records, MetaTable metaTable){
        int tableNumber = tableHeader.getTableNumber();

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(table_file.getPath(), "rw");

            for(Record record : records){
                // TODO check if the primary key is boolean

                // loop through each coordinate pointer to find the proper page
                ArrayList<Coordinate> coordinates = tableHeader.getCoordinates();
                boolean inserted = false;

                for(int j = 0; j < coordinates.size(); j++){
                    Coordinate coordinate = coordinates.get(j);
                    randomAccessFile.seek(coordinate.getOffset());
                    byte[] pageBytes = new byte[coordinate.getLength()];
                    randomAccessFile.readFully(pageBytes);

                    Page page = Page.deserialize(pageBytes, metaTable, tableNumber);
                    this.pageBuffer.putPage(page); // not sure if we really need this

                    ArrayList<Record> currentPageRecords = page.getRecords();

                    for(int i = 0; i < currentPageRecords.size(); i++){
                        Record currentPageRecord = currentPageRecords.get(i);
                        Object value = currentPageRecord.getPrimaryKey().getValue();
                        Object recordValue = record.getPrimaryKey().getValue();

                        if (value instanceof String){
                            if(((String) value).compareTo((String)recordValue) < 0){
                                // if record's string is greater than current record
                                insertRecord(record, page, i);
                                inserted = true;
                                break;
                            }
                        } else if (value instanceof Integer){
                            if ((int) value < (int) recordValue){
                                // if record's int is greater than current record
                                insertRecord(record, page, i);
                                inserted = true;
                                break;
                            }
                        } else {
                            if ((double) value < (double) recordValue){
                                // record's double is bigger
                                insertRecord(record, page, i);
                                inserted = true;
                                break;
                            }
                        }
                    }
                    if (inserted){
                        break;
                    }
                    if (j == coordinates.size() - 1){
                        // record is not placed and it's last page
                        insertRecord(record, page, page.getNumOfRecords());
                    }
                }
            }

            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void insertRecord(Record record, Page page, int index){
        return;
    }

    public void createNewCatalog() {
        this.catalog = new Catalog(this.pageSize);
    }

    public void updateCatalog() {
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

    public File getTableFile(String table) {
        String table_path = db.getName() + "/" + table;
        return new File(table_path);
    }
}
