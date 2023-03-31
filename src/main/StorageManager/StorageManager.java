package main.StorageManager;

import static main.Constants.Constant.PrepareResult.PREPARE_SUCCESS;
import static main.Constants.Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import main.Constants.CommandLineTable;
import main.Constants.Constant;
import main.Constants.Constant.DataType;
import main.Constants.Coordinate;
import main.SqlParser.ShuntingYardAlgorithm;
import main.StorageManager.Data.Attribute;
import main.StorageManager.Data.Page;
import main.StorageManager.Data.Record;
import main.StorageManager.Data.TableHeader;
import main.StorageManager.MetaData.Catalog;
import main.StorageManager.MetaData.MetaAttribute;
import main.StorageManager.MetaData.MetaTable;

public class StorageManager {

    private final File db;
    private final int bufferSize;
    private int pageSize;
    private Catalog catalog;
    private PageBuffer pageBuffer;

    public StorageManager(File db, int pageSize, int bufferSize) {
        this.db = db;
        this.pageSize = pageSize;
        this.bufferSize = bufferSize;
    }

    /**
     * Helper function for checking directory If directory exist or created, return file else system
     * exist
     */
    public void initializeDB() {
        System.out.println("Welcome to simpleDB");
        System.out.printf("Looking at %s for existing db....\n", db.getPath());
        if (!(db.exists() && db.isDirectory())) {
            System.out.println("No existing db found");
            System.out.printf("Creating new db at %s\n", db.getPath());
            if (db.mkdirs()) {
                System.out.println("New db created successfully");
            } else {
                System.out.printf("Database could not be created at %s\n", db.getPath());
                System.exit(1);
            }
        } else {
            System.out.println("Database found...");
            System.out.println("Restarting the database...");
        }
        checkCatalog();
        System.out.println("\nPlease enter commands, enter <quit> to shutdown the db\n");
    }

    private void checkCatalog() {
        File catalog_file = new File(db.getPath() + Constant.CATALOG_FILE);
        boolean restart = catalog_file.exists();
        if (restart) {
            catalog = parseCatalog(catalog_file);
            if (pageSize != catalog.getPageSize()) {
                pageSize = catalog.getPageSize();
                System.out.println("\tIgnoring provided pages size, using stored page size");
            }
        } else {
            try {
                if (catalog_file.createNewFile()) {
                    catalog = createNewCatalog();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        pageBuffer = new PageBuffer(bufferSize, pageSize, db, catalog);
        System.out.printf("Page size: %s\n", pageSize);
        System.out.printf("Buffer size: %s\n", bufferSize);
        if (restart) {
            System.out.println("\nDatabase restarted successfully");
        }
    }

    public Constant.PrepareResult executeDelete(String tableName, String conditions) {
        // Check if the file exist in the directory
        File table_file = getTableFile(tableName);
        if (!table_file.exists()) {
            System.out.printf("No such table %s\n", tableName);
            return PREPARE_UNRECOGNIZED_STATEMENT;
        }
        TableHeader tableHeader = TableHeader.parseTableHeader(table_file, pageSize);
        ArrayList<Coordinate> coordinates = Objects.requireNonNull(tableHeader)
            .getCoordinates();
        Queue<String> whereClause = ShuntingYardAlgorithm.parse(conditions);
        for (int i = 0; i < coordinates.size(); i++) {
            Page page = pageBuffer.getPage(i, tableHeader);
            ArrayList<Record> records = page.getRecords();
            ArrayList<Record> recordsToDelete = new ArrayList<>();
            for (Record record : records) {
                if (ShuntingYardAlgorithm.evaluate(new LinkedList<>(whereClause), record)) {
                    recordsToDelete.add(record);
                }
            }
            for (Record record : recordsToDelete) {
                pageBuffer.deleteRecord(record, page, i, tableHeader);
            }
        }

        return PREPARE_SUCCESS;
    }

    public Constant.PrepareResult executeAlter(String tableName, String action, String[] values) {
        // Check if the file exist in the directory
        File table_file = getTableFile(tableName);
        if (!table_file.exists()) {
            System.out.printf("No such table %s\n", tableName);
            return PREPARE_UNRECOGNIZED_STATEMENT;
        }
        // Attribute in question to be added or dropped
        MetaAttribute attribute = null;
        Object defaultValue = null;
        if (action.equals(Constant.DROP)) {
            if (values.length != 1) {
                System.out.println("Incorrect number of arguments!");
                return PREPARE_UNRECOGNIZED_STATEMENT;
            }
            String aName = values[0];
            TableHeader tableHeader = TableHeader.parseTableHeader(table_file, pageSize);
            assert tableHeader != null;
            for (MetaAttribute a : catalog.getMetaTable(tableHeader.getTableNumber())
                .metaAttributes()) {
                if (a.getName().equals(aName)) {
                    if (a.getIsPrimaryKey()) {
                        System.out.println("Can't drop primarykey");
                        return PREPARE_UNRECOGNIZED_STATEMENT;
                    }
                    attribute = a;
                }
            }
        } else {
            String aName = values[0]; // attribute name
            if (values[1].toUpperCase().contains(DataType.VARCHAR.toString())
                || values[1].toUpperCase().contains(DataType.CHAR.toString())) {
                DataType dataType = DataType.CHAR;
                if (values[1].toUpperCase().equals(DataType.VARCHAR.toString())) {
                    dataType = DataType.VARCHAR;
                }
                String s = String.join(" ", Arrays.copyOfRange(values, 1, values.length));
                int openIndex = s.indexOf('(');
                int closeIndex = s.indexOf(')');
                if (openIndex == -1 || closeIndex == -1) {
                    System.out.println("Missing Parenthesis");
                    return PREPARE_UNRECOGNIZED_STATEMENT;
                }

                // varchar( 20 )
                String typeSize = s.substring(openIndex + 1, closeIndex).trim();
                int typeLength;
                try {
                    typeLength = Integer.parseInt(typeSize);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid Length for Varchar || char");
                    return PREPARE_UNRECOGNIZED_STATEMENT;
                }
                attribute = new MetaAttribute(false, aName, dataType, typeLength, null);
                s = s.substring(closeIndex + 1).trim();
                if (!s.equals("")) {
                    String[] field = s.split(" ");
                    // "hello world"
                    if (field[0].equalsIgnoreCase(Constant.DEFAULT)) {
                        s = String.join(" ", field);
                        s = s.substring(8).trim();
                        int openQuote = s.indexOf("\"");
                        int closeQuote = s.lastIndexOf("\"");
                        if (!(openQuote == 0 && closeQuote == s.length() - 1)) {
                            System.out.println("Invalid Default value");
                            return PREPARE_UNRECOGNIZED_STATEMENT;
                        }
                        // Everything inside the quotation (exclude the quote)
                        defaultValue = s.substring(openQuote, closeQuote + 1);
                    } else {
                        System.out.println("Incorrect Default syntax");
                        return PREPARE_UNRECOGNIZED_STATEMENT;
                    }
                }
            } else {
                String type = values[1];
                if (!type.matches("(?i)INTEGER|DOUBLE|BOOLEAN")) {
                    System.out.println("Invalid Datatype");
                    return PREPARE_UNRECOGNIZED_STATEMENT;
                }
                attribute = new MetaAttribute(false, aName, DataType.valueOf(type.toUpperCase()),
                    null);

                if (values.length > 2 && values[2].equalsIgnoreCase(Constant.DEFAULT)
                    && values.length == 4) {
                    defaultValue = values[3];
                }

            }
        }

        TableHeader tableHeader = TableHeader.parseTableHeader(table_file, pageSize);
        MetaTable metaTable = this.catalog.getMetaTable(tableHeader.getTableNumber());
        ArrayList<MetaAttribute> metaAttributes = new ArrayList<>(metaTable.metaAttributes());

        if (action.equals(Constant.DROP)) {
            metaAttributes.remove(attribute);
        } else {
            metaAttributes.add(attribute);
        }

        String[] newAttribute = new String[metaAttributes.size()];
        for (int i = 0; i < newAttribute.length; i++) {
            newAttribute[i] = metaAttributes.get(i).convertString();
        }

        createTable(Constant.TEMP, newAttribute);
        ArrayList<String[]> results = pageBuffer.copyRecords(table_file, attribute, defaultValue,
            action, metaTable);
        for (String[] result : results) {
            executeInsert(Constant.TEMP, result);
        }
        executeDrop(tableName);
        TableHeader tableHeader1 = TableHeader.parseTableHeader(getTableFile(Constant.TEMP),
            pageSize);
        int tempNumber = tableHeader1.getTableNumber();
        this.catalog.getMetaTable(tempNumber).changeName(table_file.getName());
        getTableFile(Constant.TEMP).renameTo(table_file);

        return PREPARE_SUCCESS;
    }

    // create table foo( num integer primarykey, name varchar(1));
    public Constant.PrepareResult createTable(String table_name, String[] values) {
        Set<String> possible_constraints = Constant.getConstraints();

        ArrayList<MetaAttribute> attributes = new ArrayList<>();
        HashSet<String> seenAttributeNames = new HashSet<>();
        boolean foundPrimaryKey = false;
        for (String value : values) {
            Set<String> constraints = new HashSet<>();
            value = value.trim();
            String[] valArray = value.split(" ");
            boolean isPrimary = false;
            if (valArray.length < 2) {
                System.out.println("Missing fields for table attribute!");
                return PREPARE_UNRECOGNIZED_STATEMENT;
            } else {
                for (int i = 2; i < valArray.length; i++) {
                    if (!possible_constraints.contains(valArray[i]) && !valArray[i].equals(
                        "primarykey")) {
                        System.out.println("Duplicate/Invalid Field for attribute");
                        return PREPARE_UNRECOGNIZED_STATEMENT;
                    }

                    if (valArray[i].equals("primarykey")) {
                        if (foundPrimaryKey) {
                            System.out.println("Found more than one primary key!");
                            return PREPARE_UNRECOGNIZED_STATEMENT;
                        }
                        isPrimary = true;
                        foundPrimaryKey = true;
                    } else {
                        possible_constraints.remove(valArray[i]);
                        constraints.add(valArray[i]);
                    }
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
                    attributes.add(
                        new MetaAttribute(isPrimary, valArray[0].toLowerCase(), type, constraints));
                } else if (valArray[1].matches("(?i)CHAR\\([0-9]+\\)|VARCHAR\\([0-9]+\\)")) {
                    String[] typeArray = valArray[1].split("\\(");
                    Constant.DataType type = Constant.DataType.CHAR;
                    if (typeArray[0].equalsIgnoreCase("VARCHAR")) {
                        type = Constant.DataType.VARCHAR;
                    }
                    int length = Integer.parseInt(typeArray[1].replace(")", ""));
                    attributes.add(
                        new MetaAttribute(isPrimary, valArray[0].toLowerCase(), type, length,
                            constraints));
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
            System.out.printf("Table of name %s already exists\n", table_name);
            return PREPARE_UNRECOGNIZED_STATEMENT;
        }

        int tableNumber = this.catalog.getNextTableNumber();
        this.catalog.addMetaTable(table_name, attributes);

        createFile(file, tableNumber);

        return PREPARE_SUCCESS;
    }

    public Constant.PrepareResult executeSelect(String[] attributes, String table,
        Queue<String> whereAttributes, String orderByColumn) {
        // Check if the file exist in the directory
        File table_file = getTableFile(table);
        if (table_file.exists()) {
            ArrayList<Page> pages = new ArrayList<>();
            TableHeader tableHeader = TableHeader.parseTableHeader(table_file, pageSize);
            ArrayList<Coordinate> coordinates = Objects.requireNonNull(tableHeader)
                .getCoordinates();

            for (int i = 0; i < coordinates.size(); i++) {
                pages.add(pageBuffer.getPage(i, tableHeader));
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
                        if (attribute.getValue() == null) {
                            row.add("null");
                        } else {
                            row.add(attribute.getValue().toString());
                        }
                    }
                    output.addRow(row);
                }
            }
            output.print();
            return PREPARE_SUCCESS;
        } else {
            System.out.printf("No such table %s\n", table);
            return PREPARE_UNRECOGNIZED_STATEMENT;
        }
    }

    public Constant.PrepareResult executeInsert(String table, String[] values) {

        File table_file = getTableFile(table);
        if (!table_file.exists()) {
            System.out.printf("No such table %s\n", table);
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
            Constant.PrepareResult result = this.pageBuffer.findRecordPlacement(table_file, records,
                metaTable, tableHeader);

            if (records.size() != values.length || result.equals(PREPARE_UNRECOGNIZED_STATEMENT)) {
                return PREPARE_UNRECOGNIZED_STATEMENT;
            } else {
                return PREPARE_SUCCESS;
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return PREPARE_UNRECOGNIZED_STATEMENT;
        }
    }

    /**
     * Function to execute the drop table command. Verify of table exists then delete pointers and
     * from catalog
     *
     * @param table table name from user command
     * @return
     */
    public Constant.PrepareResult executeDrop(String table) {

        File table_file = getTableFile(table);
        if (!table_file.exists()) {
            System.out.printf("No such table %s\n", table);
            return PREPARE_UNRECOGNIZED_STATEMENT;
        }

        TableHeader tableHeader = TableHeader.parseTableHeader(table_file, pageSize);
        if (tableHeader == null) {
            System.out.println("Header couldn't be parsed");
            return PREPARE_UNRECOGNIZED_STATEMENT;
        }

        // Get the table number and get the schema table from catalog
        int tableNumber = tableHeader.getTableNumber();
        this.catalog.deleteMetaTable(tableNumber);
        this.pageBuffer.deletePagesFromTable(tableNumber);

        // Delete file
        table_file.delete();
        return PREPARE_SUCCESS;
    }

    private ArrayList<Record> parseRecords(String[] values, MetaTable metaTable) {

        ArrayList<MetaAttribute> metaAttributes = metaTable.metaAttributes();
        ArrayList<Record> result = new ArrayList<>();

        int index = 0;
        while (index < values.length) {
            ArrayList<Attribute> attributes = new ArrayList<>();
            List<String> matchList = new ArrayList<String>();
            Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
            Matcher regexMatcher = regex.matcher(values[index++]);
            while (regexMatcher.find()) {
                matchList.add(regexMatcher.group());
            }
            String[] retrievedAttributes = new String[matchList.size()];
            retrievedAttributes = matchList.toArray(retrievedAttributes);

            for (int i = 0; i < metaTable.metaAttributes().size(); i++) {
                MetaAttribute metaAttribute = metaTable.metaAttributes().get(i);
                String object = retrievedAttributes[i];
                DataType dataType = metaAttribute.getType();
                Set<String> constraints = metaAttribute.getConstraints();
                boolean unique = false;

                if (constraints.contains("notnull") && object.equals("null")) {
                    System.out.println("Invalid value: value can't be null for this column");
                    return result;
                }

                if (constraints.contains("unique")) {
                    unique = true;
                }

                if (object.equalsIgnoreCase("null")) {
                    attributes.add(new Attribute(metaAttribute, null));
                } else {
                    switch (dataType) {
                        case INTEGER -> {
                            int intObject;
                            try {
                                intObject = Integer.parseInt(object);
                            } catch (NumberFormatException e) {
                                System.out.printf("Invalid value: \"%s\" for Integer Type\n",
                                    object);
                                return result;
                            }
                            if (unique) {
                                if (!pageBuffer.checkUnique(getTableFile(metaTable.getTableName()),
                                    intObject, metaTable, i)) {
                                    System.out.println(
                                        "Invalid value: value is unique and already exist");
                                    return result;
                                }
                            }
                            attributes.add(new Attribute(metaAttribute, intObject));
                        }
                        case DOUBLE -> {
                            double doubleObject;
                            try {
                                doubleObject = Double.parseDouble(object);
                            } catch (NumberFormatException e) {
                                System.out.printf("Invalid value: \"%s\" for Double Type\n",
                                    object);
                                return result;
                            }
                            if (unique) {
                                if (!pageBuffer.checkUnique(getTableFile(metaTable.getTableName()),
                                    doubleObject, metaTable, i)) {
                                    System.out.println(
                                        "Invalid value: value is unique and already exist");
                                    return result;
                                }
                            }
                            attributes.add(new Attribute(metaAttribute, doubleObject));
                        }
                        case BOOLEAN -> {
                            if (object.equalsIgnoreCase("true")) {
                                if (unique) {
                                    if (!pageBuffer.checkUnique(
                                        getTableFile(metaTable.getTableName()),
                                        true, metaTable, i)) {
                                        System.out.println(
                                            "Invalid value: value is unique and already exist");
                                        return result;
                                    }
                                }
                                attributes.add(new Attribute(metaAttribute, true));
                            } else if (object.equalsIgnoreCase("false")) {
                                if (unique) {
                                    if (!pageBuffer.checkUnique(
                                        getTableFile(metaTable.getTableName()),
                                        false, metaTable, i)) {
                                        System.out.println(
                                            "Invalid value: value is unique and already exist");
                                        return result;
                                    }
                                }
                                attributes.add(new Attribute(metaAttribute, false));
                            } else {
                                System.out.printf("Invalid value: \"%s\" for Boolean Type\n",
                                    object);
                                return result;
                            }
                        }
                        case CHAR, VARCHAR -> {
                            if (object.charAt(0) != '\"'
                                || object.charAt(object.length() - 1) != '\"') {
                                System.out.printf("Invalid value: %s, missing quotes\n", object);
                                return result;
                            }
                            object = object.substring(1, object.length() - 1);
                            if (object.length() > metaAttribute.getMaxLength()) {
                                System.out.printf("\"%s\" length exceeds %s(%d)\n", object,
                                    dataType.name(), metaAttribute.getMaxLength());
                                return result;
                            }
                            if (unique) {
                                if (!pageBuffer.checkUnique(getTableFile(metaTable.getTableName()),
                                    object, metaTable, i)) {
                                    System.out.println(
                                        "Invalid value: value is unique and already exist");
                                    return result;
                                }
                            }
                            attributes.add(
                                new Attribute(metaAttribute, object));
                        }
                    }
                }
            }
            Record record = new Record(attributes, metaAttributes);
            result.add(record);
        }
        return result;
    }

    /**
     * Prints specified table schema.
     *
     * @param table - table name
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

        int numOfPages = Objects.requireNonNull(tableHeader).getCoordinates().size();
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
        System.out.println("Tables:");
        for (MetaTable metaTable : catalog.getMetaTableHashMap().values()) {
            System.out.print("\n" + metaTable.toString());
            int numOfPages = 0;
            int numOfRecords = 0;
            File table_file = getTableFile(metaTable.getTableName());
            if (table_file.exists()) {
                TableHeader tableHeader = TableHeader.parseTableHeader(table_file, pageSize);
                numOfPages = Objects.requireNonNull(tableHeader).getCoordinates().size();
                numOfRecords = tableHeader.getTotalRecords(table_file, this.pageBuffer, metaTable,
                    this.pageSize);
            }
            System.out.format("Pages: %s\n", numOfPages);
            System.out.format("Records: %s\n", numOfRecords);
        }
        return PREPARE_SUCCESS;
    }

    public Catalog createNewCatalog() {
        return new Catalog(this.pageSize);
    }

    public void saveCatalog() {
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

    private Catalog parseCatalog(File catalog_file) {
        // Deserialize the file and return a catalog
        try (RandomAccessFile raf = new RandomAccessFile(catalog_file, "rw")) {
            int fileLength = (int) raf.length();
            byte[] bytes = new byte[fileLength];
            raf.readFully(bytes);
            return Catalog.deserialize(bytes);
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
            byte[] bytes = tableHeader.serialize();
            randomAccessFile.write(bytes);
            randomAccessFile.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void saveData() {
        System.out.println("Purging page buffer...");
        this.pageBuffer.updateAllPage();

        System.out.println("Saving catalog...");
        saveCatalog();
    }
}
