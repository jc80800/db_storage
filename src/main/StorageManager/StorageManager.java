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

import static main.Constants.Constant.PrepareResult.PREPARE_SUCCESS;
import static main.Constants.Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;

public class StorageManager {

    private final File db;
    private final int pageSize;
    private final int bufferSize;
    private Catalog catalog;
    private PageBuffer pageBuffer;

    public StorageManager(File db, int pageSize, int bufferSize) {
        this.pageBuffer = new PageBuffer(bufferSize, pageSize, db, this.catalog);
        this.db = db;
        this.pageSize = pageSize;
        this.bufferSize = bufferSize;
    }

    public Constant.PrepareResult createTable(String table_name, String[] values) {
        ArrayList<MetaAttribute> attributes = new ArrayList<>();
        HashSet<String> seenAttributeNames = new HashSet<>();
        boolean foundPrimaryKey = false;
        for (String value : values) {
            value = value.trim();
            String[] valArray = value.split(" ");
            boolean isPrimary = false;
            if (valArray.length < 2) {
                System.out.println("ERROR: Missing fields for table attribute!");
                return PREPARE_UNRECOGNIZED_STATEMENT;
            } else if (valArray.length > 3 || (valArray.length == 3
                && !valArray[2].equalsIgnoreCase("primarykey"))) {
                System.out.println("ERROR: Too many fields found for table attribute!");
                return PREPARE_UNRECOGNIZED_STATEMENT;
            } else {
                if (valArray.length == 3 && foundPrimaryKey) {
                    System.out.println("ERROR: Found more than one primary key!");
                    return PREPARE_UNRECOGNIZED_STATEMENT;
                } else if (valArray.length == 3) {
                    isPrimary = true;
                    foundPrimaryKey = true;
                }

                if (seenAttributeNames.contains(valArray[0].toLowerCase())) {
                    System.out.println("ERROR: One or more attributes have the same name!");
                    return PREPARE_UNRECOGNIZED_STATEMENT;
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
                    System.out.println("ERROR: Syntax error was found!");
                    return PREPARE_UNRECOGNIZED_STATEMENT;
                }
            }
        }

        if (!foundPrimaryKey) {
            System.out.println("ERROR: No primary key was specified!");
            return PREPARE_UNRECOGNIZED_STATEMENT;
        }

        int tableNumber = this.catalog.getNextTableNumber();
        this.catalog.addMetaTable(table_name, attributes);

        if(createFile(table_name, tableNumber)){
            System.out.println("Table file created");
        } else {
            System.out.println("Table couldn't be created / Exist already");
        }

        return PREPARE_SUCCESS;
    }

    public void executeSelect(String table) {
        // Check if the file exist in the directory
        File table_file = getTableFile(table);
        if (table_file.exists()) {
            ArrayList<Page> pages = new ArrayList<>();
            TableHeader tableHeader = TableHeader.parseTableHeader(table_file);
            ArrayList<Coordinate> coordinates = tableHeader.getCoordinates();

            for (int i = 0; i < coordinates.size(); i++) {
                if (!pageBuffer.pages.containsKey(i)) {
                    //get page from file and put into buffer
                    try {
                        RandomAccessFile randomAccessFile = new RandomAccessFile(
                            table_file.getPath(), "r");
                        randomAccessFile.seek(coordinates.get(i).getOffset());
                        byte[] pageBytes = new byte[catalog.getPageSize()];
                        randomAccessFile.readFully(pageBytes);

                        Page page = Page.deserialize(pageBytes,
                            catalog.getMetaTable(tableHeader.getTableNumber()),
                            tableHeader.getTableNumber(), catalog.getPageSize(), i);

                        pageBuffer.putPage(page);
                        randomAccessFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                pages.add(pageBuffer.getPage(i));
            }
            for (Page page : pages) {
                for(Record record: page.getRecords()){
                    System.out.println(record);
                }
            }
        } else {
            System.out.println("Table doesn't exist");
        }
    }

    public Constant.PrepareResult executeInsert(String table, String[] values) {

        File table_file = getTableFile(table);
        TableHeader tableHeader = TableHeader.parseTableHeader(table_file);
        if (!table_file.exists()) {
            System.out.println("Table doesn't exist");
            return PREPARE_UNRECOGNIZED_STATEMENT;
        }

        if (tableHeader == null) {
            System.out.println("Header couldn't be parsed");
            return PREPARE_UNRECOGNIZED_STATEMENT;
        }

        // Get the table number and get the schema table from catalog
        int tableNumber = tableHeader.getTableNumber();
        MetaTable metaTable = this.catalog.getMetaTable(tableNumber);

        // Parse the values from user and validate it
        ArrayList<Record> records = Record.parseRecords(values, metaTable);

        // Find where to place each record and place it
        findRecordPlacement(table_file, records, metaTable, tableHeader);

        System.out.println("Insertion Completed");

        return PREPARE_SUCCESS;
    }

    /**
     * TODO need to test if it works after populating with create table and such
     *
     * @param table
     */
    public void displayInfo(String table) {
        boolean foundTable = false;
        MetaTable foundMetaTable = null;
        for (MetaTable metaTable : catalog.getMetaTableHashMap().values()) {
            if (metaTable.getTableName().equalsIgnoreCase(table)) {
                foundTable = true;
                foundMetaTable = metaTable;
                System.out.print(metaTable);
                break;
            }
        }
        if (!foundTable) {
            System.out.format("No such table %s\n", table);
            return;
        }
        File table_file = getTableFile(table);
        TableHeader tableHeader = TableHeader.parseTableHeader(table_file);

        int numOfPages = tableHeader.getCoordinates().size();
        int numOfRecords = tableHeader.getTotalRecords(table_file, this.pageBuffer, foundMetaTable, this.pageSize);

        System.out.format("Pages: %d\n", numOfPages);
        System.out.format("Records: %d\n", numOfRecords);
    }

    /**
     * prints: database location, page size, buffer size, table schema
     */
    public void displaySchema() {
        System.out.format("DB location: %s\n", db.getPath());
        System.out.format("Page size: %s\n", this.pageSize);
        System.out.format("Buffer size: %s\n\n", this.bufferSize);
        int numOfTables = catalog.getTableSize();
        if (numOfTables == 0) {
            System.out.println("No tables to display");
            return;
        }
        System.out.println("Tables:\n");
        for (MetaTable metaTable : catalog.getMetaTableHashMap().values()) {
            System.out.print(metaTable.toString());
            int numOfPages = 0;
            int numOfRecords = 0;
            File table_file = getTableFile(metaTable.getTableName());
            if (table_file.exists()) {
                try (RandomAccessFile randomAccessFile = new RandomAccessFile(table_file.getPath(),
                    "rw")) {
                    byte[] bytes = new byte[8];
                    randomAccessFile.readFully(bytes, Constant.INTEGER_SIZE,
                        Constant.INTEGER_SIZE * 2);
                    numOfPages = Helper.convertByteArrayToInt(Arrays.copyOf(bytes, 4));
                    numOfRecords = Helper.convertByteArrayToInt(Arrays.copyOfRange(bytes, 4, 8));
                } catch (IOException e) {
                    throw new RuntimeException(
                        String.format("Can not access file %s", table_file.getPath()));
                }
            }
            System.out.format("Pages: %s\n", numOfPages);
            System.out.format("Records: %s\n\n", numOfRecords);
        }
    }

    public void findRecordPlacement(File table_file,
        ArrayList<Record> records, MetaTable metaTable, TableHeader tableHeader) {
        int tableNumber = tableHeader.getTableNumber();

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(table_file.getPath(), "rw");

            if (tableHeader.getCoordinates().size() == 0) {
                this.pageBuffer.putPage(tableHeader.createFirstPage(this.pageSize));
            }

            ArrayList<Coordinate> coordinates = tableHeader.getCoordinates();

            for (Record record : records) {
                Boolean inserted;

                for (int i = 0; i < coordinates.size(); i++) {
                    Page page = pageBuffer.getPage(i);
                    if (page == null) {
                        // If the page Buffer doesn't have it, go to the file and deserialize
                        byte[] bytes = new byte[this.pageSize];
                        randomAccessFile.seek(coordinates.get(i).getOffset());
                        randomAccessFile.readFully(bytes);
                        page = Page.deserialize(bytes, metaTable, tableNumber, this.pageSize, i);
                        this.pageBuffer.putPage(page);
                    }

                    ArrayList<Record> pageRecords = page.getRecords();

                    // Check if record can be placed in this page
                    inserted = checkPlacement(pageRecords, record, page, tableHeader);
                    if (inserted == null) {
                        System.out.println("PK already exist");
                        return;
                    } else if (inserted) {
                        break;
                    }

                    if (i == coordinates.size() - 1) {
                        // If no place, insert at the very end
                        insertRecord(record, page, pageRecords.size(), tableHeader);
                    }
                }
            }
            randomAccessFile.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Boolean checkPlacement(ArrayList<Record> records, Record target, Page page, TableHeader tableHeader) {
        Object recordValue = target.getPrimaryKey().getValue();

        for (int i = 0; i < records.size(); i++) {
            Record currentPageRecord = records.get(i);
            Object value = currentPageRecord.getPrimaryKey().getValue();

            if (value instanceof String) {
                if (((String) value).compareTo((String) recordValue) == 0) {
                    return null;
                }

                if (((String) value).compareTo((String) recordValue) < 0) {
                    // if record's string is greater than current record
                    insertRecord(target, page, i, tableHeader);
                    return true;
                }
            } else if (value instanceof Boolean) {
                if ((boolean) value == (boolean) recordValue) {
                    return null;
                }
            } else if (value instanceof Integer) {
                if ((int) value == (int) recordValue) {
                    return null;
                }
                if ((int) value < (int) recordValue) {
                    // if record's int is greater than current record
                    insertRecord(target, page, i, tableHeader);
                    return true;
                }
            } else {
                if ((double) value == (double) recordValue) {
                    return null;
                }
                if ((double) value < (double) recordValue) {
                    // record's double is bigger
                    insertRecord(target, page, i, tableHeader);
                    return true;
                }
            }
        }
        return false;
    }


    public void insertRecord(Record record, Page page, int index, TableHeader tableHeader) {
        Page potentialNewPage = page.insertRecord(record, index, tableHeader);
        if (potentialNewPage != null){
            this.pageBuffer.putPage(potentialNewPage);
        }
    }

    public void createNewCatalog() {
        this.catalog = new Catalog(this.pageSize);
        this.pageBuffer.updateCatalog(this.catalog);
        saveCatalog();
    }

    public void saveCatalog() {
        System.out.println("Saving catalog...");
        File catalog_file = new File(this.db + Constant.CATALOG_FILE);
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(catalog_file,
            "rw")) {
            byte[] bytes = this.catalog.serialize();
            // erase current content
            randomAccessFile.setLength(0);
            randomAccessFile.write(bytes);
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
            this.pageBuffer.updateCatalog(this.catalog);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getTableFile(String table) {
        String table_path = db.getName() + "/" + table;
        return new File(table_path);
    }

    public boolean createFile(String table, int tableNumber){
        File file = getTableFile(table);

        TableHeader tableHeader = new TableHeader(tableNumber, file);
        try {
            if(file.createNewFile()){
                RandomAccessFile randomAccessFile = new RandomAccessFile(file.getPath(), "rw");
                System.out.println("Writing Table Header");
                byte[] bytes = tableHeader.serialize();
                randomAccessFile.write(bytes);
                randomAccessFile.close();
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void saveData(){
        saveCatalog();
        this.pageBuffer.updateAllPage();
        checkLength(new File(this.db.getPath() + "/foo"));
    }

    public static void checkLength(File file) {
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file.getPath(), "rw");
            byte[] bytes = new byte[(int) randomAccessFile.length()];
            randomAccessFile.readFully(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
