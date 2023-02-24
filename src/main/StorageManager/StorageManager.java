package main.StorageManager;

import static main.Constants.Constant.PrepareResult.PREPARE_SUCCESS;
import static main.Constants.Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import main.Constants.CommandLineTable;
import main.Constants.Constant;
import main.Constants.Constant.DataType;
import main.Constants.Coordinate;
import main.StorageManager.Data.Attribute;
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
    private final PageBuffer pageBuffer;

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
                System.out.println("Missing fields for table attribute!");
                return PREPARE_UNRECOGNIZED_STATEMENT;
            } else if (valArray.length > 3 || (valArray.length == 3
                && !valArray[2].equalsIgnoreCase("primarykey"))) {
                System.out.println("Too many fields found for table attribute!");
                return PREPARE_UNRECOGNIZED_STATEMENT;
            } else {
                if (valArray.length == 3 && foundPrimaryKey) {
                    System.out.println("Found more than one primary key!");
                    return PREPARE_UNRECOGNIZED_STATEMENT;
                } else if (valArray.length == 3) {
                    isPrimary = true;
                    foundPrimaryKey = true;
                }

                if (seenAttributeNames.contains(valArray[0].toLowerCase())) {
                    System.out.println("One or more attributes have the same name!");
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
                    System.out.println("Syntax error was found!");
                    return PREPARE_UNRECOGNIZED_STATEMENT;
                }
            }
        }

        if (!foundPrimaryKey) {
            System.out.println("No primary key was specified!");
            return PREPARE_UNRECOGNIZED_STATEMENT;
        }

        File file = getTableFile(table_name);
        if (file.exists()) {
            System.out.println("Table couldn't be created / Exist already");
            return PREPARE_UNRECOGNIZED_STATEMENT;
        }

        int tableNumber = this.catalog.getNextTableNumber();
        this.catalog.addMetaTable(table_name, attributes);

        createFile(file, tableNumber);
        System.out.println("Table file created");

        return PREPARE_SUCCESS;
    }

    public Constant.PrepareResult executeSelect(String table) {
        // Check if the file exist in the directory
        File table_file = getTableFile(table);
        if (table_file.exists()) {
            ArrayList<Page> pages = new ArrayList<>();
            TableHeader tableHeader = TableHeader.parseTableHeader(table_file, pageSize);
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
            CommandLineTable output = new CommandLineTable();

            ArrayList<String> header = new ArrayList<>();
            for (MetaAttribute attribute : catalog.getMetaTable(tableHeader.getTableNumber())
                .metaAttributes()) {
                header.add(attribute.getName());
            }
            output.setHeaders(header.toArray(new String[0]));

            for (Page page : pages) {
                for (Record record : page.getRecords()) {
                    ArrayList<String> row = new ArrayList<>();
                    for (Attribute attribute : record.getAttributes()) {
                        row.add(attribute.getValue().toString());
                    }
                    output.addRow(row);
                }
            }
            output.print();
            return PREPARE_SUCCESS;
        } else {
            System.out.println("Table doesn't exist");
            return PREPARE_UNRECOGNIZED_STATEMENT;
        }
    }

    public Constant.PrepareResult executeInsert(String table, String[] values) {

        File table_file = getTableFile(table);
        if (!table_file.exists()) {
            System.out.println("Table doesn't exist");
            return PREPARE_UNRECOGNIZED_STATEMENT;
        }

        TableHeader tableHeader = TableHeader.parseTableHeader(table_file, pageSize);
        if (tableHeader == null) {
            System.out.println("Header couldn't be parsed");
            return PREPARE_UNRECOGNIZED_STATEMENT;
        }

        // Get the table number and get the schema table from catalog
        int tableNumber = tableHeader.getTableNumber();
        MetaTable metaTable = this.catalog.getMetaTable(tableNumber);

        // Parse the values from user and validate it
        try {
            ArrayList<Record> records = parseRecords(values, metaTable);

            // Find where to place each record and place it
            return this.pageBuffer.findRecordPlacement(table_file, records, metaTable, tableHeader);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return PREPARE_UNRECOGNIZED_STATEMENT;
        }
    }

    private ArrayList<Record> parseRecords(String[] values, MetaTable metaTable) {

        ArrayList<MetaAttribute> metaAttributes = metaTable.metaAttributes();
        ArrayList<Record> result = new ArrayList<>();

        int index = 0;
        while (index < values.length) {
            ArrayList<Attribute> attributes = new ArrayList<>();
            for (int i = 0; i < metaTable.metaAttributes().size(); i++) {
                MetaAttribute metaAttribute = metaTable.metaAttributes().get(i);
                String object = values[index++];
                DataType dataType = metaAttribute.getType();

                switch (dataType) {
                    case INTEGER -> {
                        int intObject;
                        try {
                            intObject = Integer.parseInt(object);
                        } catch (NumberFormatException e) {
                            System.out.printf("Invalid value: \"%s\" for Integer Type%n", object);
                            return result;
                        }
                        attributes.add(new Attribute(metaAttribute, intObject));
                    }
                    case DOUBLE -> {
                        double doubleObject;
                        try {
                            doubleObject = Double.parseDouble(object);
                        } catch (NumberFormatException e) {
                            System.out.printf("Invalid value: \"%s\" for Double Type%n", object);
                            return result;
                        }
                        attributes.add(new Attribute(metaAttribute, doubleObject));
                    }
                    case BOOLEAN -> {
                        if (object.equalsIgnoreCase("true")) {
                            attributes.add(new Attribute(metaAttribute, true));
                        } else if (object.equalsIgnoreCase("false")) {
                            attributes.add(new Attribute(metaAttribute, false));
                        } else {
                            System.out.printf("Invalid value: \"%s\" for Boolean Type%n", object);
                            return result;
                        }
                    }
                    case CHAR, VARCHAR -> {
                        if (object.charAt(0) != '\"'
                            || object.charAt(object.length() - 1) != '\"') {
                            System.out.printf("Invalid value: %s, missing quotes%n", object);
                            return result;
                        }
                        object = object.substring(1, object.length() - 1);
                        if (object.length() > metaAttribute.getMaxLength()) {
                            System.out.printf("\"%s\" length exceeds %s(%d)%n", object,
                                dataType.name(), metaAttribute.getMaxLength());
                            return result;
                        }
                        attributes.add(
                            new Attribute(metaAttribute, object));
                    }
                }
            }
            Record record = new Record(attributes, metaAttributes);
            result.add(record);
        }
        return result;
    }

    /**
     * TODO need to test if it works after populating with create table and such
     *
     * @param table
     */
    public Constant.PrepareResult displayInfo(String table) {
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
            return PREPARE_UNRECOGNIZED_STATEMENT;
        }
        File table_file = getTableFile(table);
        TableHeader tableHeader = TableHeader.parseTableHeader(table_file, pageSize);

        int numOfPages = tableHeader.getCoordinates().size();
        int numOfRecords = tableHeader.getTotalRecords(table_file, this.pageBuffer, foundMetaTable,
            this.pageSize);

        System.out.format("Pages: %d\n", numOfPages);
        System.out.format("Records: %d\n", numOfRecords);

        return PREPARE_SUCCESS;
    }

    /**
     * prints: database location, page size, buffer size, table schema
     */
    public Constant.PrepareResult displaySchema() {
        System.out.format("DB location: %s\n", db.getPath());
        System.out.format("Page size: %s\n", this.pageSize);
        System.out.format("Buffer size: %s\n\n", this.bufferSize);
        int numOfTables = catalog.getTableSize();
        if (numOfTables == 0) {
            System.out.println("No tables to display");
            return PREPARE_SUCCESS;
        }
        System.out.println("Tables:\n");
        for (MetaTable metaTable : catalog.getMetaTableHashMap().values()) {
            System.out.print(metaTable.toString());
            int numOfPages = 0;
            int numOfRecords = 0;
            File table_file = getTableFile(metaTable.getTableName());
            if (table_file.exists()) {
                TableHeader tableHeader = TableHeader.parseTableHeader(table_file, pageSize);
                numOfPages = tableHeader.getCoordinates().size();
                numOfRecords = tableHeader.getTotalRecords(table_file, this.pageBuffer, metaTable,
                    this.pageSize);
            }
            System.out.format("Pages: %s\n", numOfPages);
            System.out.format("Records: %s\n\n", numOfRecords);
        }
        return PREPARE_SUCCESS;
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

    public void createFile(File file, int tableNumber) {
        TableHeader tableHeader = new TableHeader(tableNumber, file, pageSize);
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file.getPath(), "rw");
            System.out.println("Writing Table Header");
            byte[] bytes = tableHeader.serialize();
            randomAccessFile.write(bytes);
            randomAccessFile.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void saveData() {
        saveCatalog();
        this.pageBuffer.updateAllPage();
    }
}
