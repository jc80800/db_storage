package main.StorageManager;

import static main.Constants.Constant.PrepareResult.PREPARE_SUCCESS;
import static main.Constants.Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.crypto.Data;
import main.Constants.CommandLineTable;
import main.Constants.Constant;
import main.Constants.Constant.DataType;
import main.Constants.Coordinate;
import main.SqlParser.ShuntingYardAlgorithm;
import main.StorageManager.B_Tree.BPlusTree;
import main.StorageManager.B_Tree.RecordPointer;
import main.StorageManager.Data.*;
import main.StorageManager.Data.Record;
import main.StorageManager.MetaData.Catalog;
import main.StorageManager.MetaData.MetaAttribute;
import main.StorageManager.MetaData.MetaTable;

public class StorageManager {

    private final File db;
    private final int bufferSize;
    private int pageSize;
    private Catalog catalog;
    private PageBuffer pageBuffer;
    private boolean isIndex;
    private HashMap<Integer, BPlusTree> bPlusTreeHashMap;

    public StorageManager(File db, int pageSize, int bufferSize, boolean isIndex) {
        this.db = db;
        this.pageSize = pageSize;
        this.bufferSize = bufferSize;
        this.isIndex = isIndex;
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
        parseBTrees();
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
        pageBuffer = new PageBuffer(bufferSize, pageSize, db, catalog, isIndex);
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
        assert tableHeader != null;

        ArrayList<Coordinate> coordinates = Objects.requireNonNull(tableHeader)
                .getCoordinates();
        Queue<String> whereClause = ShuntingYardAlgorithm.parse(conditions);
        BPlusTree bPlusTree = null;
        if(isIndex){
            String[] field = conditions.split("=");
            String value = field[1];
            int tableNumber = tableHeader.getTableNumber();
            MetaTable metaTable = this.catalog.getMetaTable(tableNumber);
            bPlusTree = bPlusTreeHashMap.get(metaTable.getTableNumber());
            Object newValue = convertValue(value.trim(), metaTable.getPrimaryKey().getType());
            RecordPointer recordPointer = bPlusTree.findRecordPointerForDeletion(newValue);
            if(recordPointer == null){
                System.out.println("RP is null??");
                return PREPARE_UNRECOGNIZED_STATEMENT;
            }

            int pageNumber = recordPointer.getPageNumber();
            int indexNumber = recordPointer.getRecordIndex();
            Page page = pageBuffer.getPageByPageId(pageNumber, tableHeader, tableHeader.getCoordinates());
            page.deleteRecordAtIndex(indexNumber, tableHeader);
            bPlusTree.delete(newValue);

        } else {
            for (int i = 0; i < coordinates.size(); i++) {
                Page page = pageBuffer.getPage(coordinates.get(i), tableHeader);
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
        }

        return PREPARE_SUCCESS;
    }

    public Constant.PrepareResult executeUpdate(String tableName, String attributeName, String newValue,
                                                String conditions) {
        File table_file = getTableFile(tableName);
        if (!table_file.exists()) {
            System.out.printf("No such table %s\n", tableName);
            return PREPARE_UNRECOGNIZED_STATEMENT;
        }
        TableHeader tableHeader = TableHeader.parseTableHeader(table_file, pageSize);
        int tableNumber = tableHeader.getTableNumber();
        MetaTable metaTable = this.catalog.getMetaTable(tableNumber);
        ArrayList<Coordinate> coordinates = Objects.requireNonNull(tableHeader)
                .getCoordinates();
        Queue<String> whereClause = ShuntingYardAlgorithm.parse(conditions);

        BPlusTree bPlusTree = null;
        if(isIndex){
            String[] field = conditions.split("=");
            Object sk = convertValue(field[1], metaTable.getPrimaryKey().getType());
            bPlusTree = bPlusTreeHashMap.get(tableHeader.getTableNumber());
            RecordPointer recordPointer = bPlusTree.findRecordPointerForDeletion(sk);
            if (recordPointer == null){
                return PREPARE_UNRECOGNIZED_STATEMENT;
            }
            int pageNumber = recordPointer.getPageNumber();
            int indexNumber = recordPointer.getRecordIndex();
            Page page = pageBuffer.getPageByPageId(pageNumber, tableHeader, tableHeader.getCoordinates());
            page.deleteRecordAtIndex(indexNumber, tableHeader);
            bPlusTree.delete(newValue);

            return executeInsert(tableName, new String[]{newValue});

        } else {

            for (int i = 0; i < coordinates.size(); i++) {
                Page page = pageBuffer.getPage(coordinates.get(i), tableHeader);
                ArrayList<Record> records = page.getRecords();
                ArrayList<Record> recordsToDelete = new ArrayList<>();
                ArrayList<Record> updatedRecords = new ArrayList<>();
                for (Record record : records) {
                    if (ShuntingYardAlgorithm.evaluate(new LinkedList<>(whereClause), record)) {
                        Record updatedRecord = new Record(record);
                        if (!record.hasAttribute(attributeName)) {
                            System.out.printf("Table %s does not have column %s\n", tableName,
                                attributeName);
                            return PREPARE_UNRECOGNIZED_STATEMENT;
                        }
                        updatedRecord.getAttributeByName(attributeName).setValue(newValue);
                        if (!pageBuffer.validateRecord(updatedRecord, record, metaTable,
                            tableHeader)) {
                            break;
                        }
                        recordsToDelete.add(record);
                        updatedRecords.add(updatedRecord);
                    }
                }

                for (Record record : recordsToDelete) {
                    pageBuffer.deleteRecord(record, page, i, tableHeader);
                }
                pageBuffer.findRecordPlacement(updatedRecords, tableHeader, bPlusTree);
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

        // Create an index
        if(this.isIndex){
            MetaTable metaTable = this.catalog.getMetaTable(tableNumber);
            File BPlusTreeFile = createIndexFile(table_name);
            BPlusTree bPlusTree = new BPlusTree(BPlusTreeFile, metaTable.getPrimaryKey(), pageSize);
            bPlusTreeHashMap.put(tableNumber, bPlusTree);
        }
        return PREPARE_SUCCESS;
    }

    public Constant.PrepareResult executeSelect(ArrayList<String> attributes, ArrayList<String> tableList,
                                                Queue<String> whereAttributes, String orderByColumn) {

        if(orderByColumn != null && !attributes.contains(orderByColumn)) {
            attributes.add(orderByColumn);
        }

        CommandLineTable output = new CommandLineTable();
        ArrayList<MetaAttribute> metaAttributes = new ArrayList<>();
        ArrayList<Record> records = new ArrayList<>();
        for (int i = 0; i < tableList.size(); i++) {
            File tableFile = getTableFile(tableList.get(i));

            if (tableFile.exists()) {
                TableHeader tableHeader = TableHeader.parseTableHeader(tableFile, pageSize);

                assert tableHeader != null;

                for (MetaAttribute attribute : catalog.getMetaTable(tableHeader.getTableNumber()).metaAttributes()) {
                    if (tableList.size() == 1) {
                        metaAttributes.add(attribute);
                    } else {
                        metaAttributes.add(new MetaAttribute(attribute.getIsPrimaryKey(), tableList.get(i) + "." + attribute.getName(), attribute.getType(), attribute.getConstraints()));
                    }
                }
                ArrayList<Coordinate> coordinates = Objects.requireNonNull(tableHeader).getCoordinates();

                ArrayList<Record> newRecords = new ArrayList<>();

                for (int j = 0; j < coordinates.size(); j++) {
                    Coordinate coordinate = coordinates.get(j);

                    Page page = pageBuffer.getPage(coordinate, tableHeader);

                    if (i == 0) {
                        newRecords.addAll(page.getRecords());
                    } else {
                        for (Record record : page.getRecords()) {
                            for (Record currRecord : records) {
                                ArrayList<Attribute> combinedAttributes = new ArrayList<>();
                                combinedAttributes.addAll(currRecord.getAttributes());
                                combinedAttributes.addAll(record.getAttributes());
                                Record combinedRecord = new Record(combinedAttributes, metaAttributes);
                                newRecords.add(combinedRecord);
                            }
                        }
                    }
                }
                records = newRecords;
            } else {
                System.out.printf("No such table %s\n", tableFile);
                return PREPARE_UNRECOGNIZED_STATEMENT;
            }
        }

        ArrayList<String> header = new ArrayList<>();
        for (int i = 0; i < metaAttributes.size(); i++) {
            if(attributes.contains("*")){
                header.add(metaAttributes.get(i).getName());
            } else {
                if (attributes.contains(metaAttributes.get(i).getName())) {
                    header.add(metaAttributes.get(i).getName());
                }
            }
        }
        output.setHeaders(header.toArray(new String[0]));

        if(whereAttributes != null){
            ArrayList<Record> result = new ArrayList<>();
            for(Record record: records){
                Queue<String> temp = new LinkedList<>(whereAttributes);
                try {
                    if (ShuntingYardAlgorithm.evaluate(temp, record)) {
                        result.add(record);
                    }
                } catch(IllegalArgumentException e){
                    return PREPARE_UNRECOGNIZED_STATEMENT;
                }
            }
            records = result;
        }

        if(orderByColumn != null){
            ArrayList<Record> records1 = new ArrayList<>();
            ArrayList<SortRecord> sortRecords = new ArrayList<>();
            for (Record record : records) {
                sortRecords.add(new SortRecord(record, orderByColumn));
            }
            Collections.sort(sortRecords);
            for(SortRecord sortRecord : sortRecords){
                records1.add(sortRecord.getRecord());
            }
            records = records1;
        }

        for (Record record : records) {
            ArrayList<String> row = new ArrayList<>();
            for (Attribute attribute : record.getAttributes()) {
                if(!attributes.contains("*")) {
                    if(attributes.contains(attribute.getMetaAttribute().getName())){
                        if (attribute.getValue() == null) {
                            row.add("null");
                        } else {
                            row.add(attribute.getValue().toString());
                        }
                    }
                } else {
                    if (attribute.getValue() == null) {
                        row.add("null");
                    } else {
                        row.add(attribute.getValue().toString());
                    }
                }

            }
            output.addRow(row);
        }
        output.print();
        return PREPARE_SUCCESS;
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

        BPlusTree bPlusTree = bPlusTreeHashMap.get(tableNumber);

        // Parse the values from user and validate it
        try {
            ArrayList<Record> records = parseRecords(values, metaTable, tableHeader);


            // Find where to place each record and place it
            Constant.PrepareResult result = this.pageBuffer.findRecordPlacement(records, tableHeader, bPlusTree);

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

    private ArrayList<Record> parseRecords(String[] values, MetaTable metaTable, TableHeader tableHeader) {

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
                Attribute attribute;
                if (object.equalsIgnoreCase("null")) {
                    attribute = new Attribute(metaAttribute, null);
                } else {
                    try {
                         attribute = new Attribute(metaAttribute, object);
                    } catch (IllegalArgumentException e) {
                        return result;
                    }
                }
                attributes.add(attribute);
            }

            Record record = new Record(attributes, metaAttributes);
            if (!pageBuffer.validateRecord(record, null, metaTable, tableHeader)) {
                throw new IllegalArgumentException();
            };

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
        for (MetaTable metaTable : catalog.getMetaTableHashMap().values()) {
            if (metaTable.getTableName().equalsIgnoreCase(table)) {
                foundTable = true;
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
        int numOfRecords = tableHeader.getTotalRecords(this.pageBuffer
        );

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
                numOfRecords = tableHeader.getTotalRecords(this.pageBuffer
                );
            }
            System.out.format("Pages: %s\n", numOfPages);
            System.out.format("Records: %s\n", numOfRecords);
        }
        return PREPARE_SUCCESS;
    }

    public Object convertValue(String searchValue, DataType dataType){
        return switch (dataType) {
            case INTEGER -> Integer.parseInt(searchValue);
            case DOUBLE -> Double.parseDouble(searchValue);
            case BOOLEAN -> Boolean.parseBoolean(searchValue);
            case VARCHAR -> searchValue;
            default -> searchValue.charAt(0);
        };
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

    private void persistBTree(BPlusTree bPlusTree) {
        try (RandomAccessFile raf = new RandomAccessFile(bPlusTree.getFile(), "rw")) {
            byte[] bytes = bPlusTree.serialize();
            raf.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void persistBTrees() {
        for (Map.Entry<Integer, BPlusTree> entry : bPlusTreeHashMap.entrySet()) {
            BPlusTree bPlusTree = entry.getValue();
            persistBTree(bPlusTree);
        }
    }

    private BPlusTree parseBtree(File file, MetaAttribute metaAttribute) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] bytes = new byte[BPlusTree.BinarySize()];
            raf.readFully(bytes);
            return BPlusTree.deserialize(bytes,file, metaAttribute, pageSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void parseBTrees() {
        HashMap<Integer, BPlusTree> bPlusTreeHashMap = new HashMap<>();
        for (Map.Entry<Integer, MetaTable> entry : this.catalog.getMetaTableHashMap().entrySet()) {
            MetaTable metaTable = entry.getValue();
            File BTreeFile = new File(db.getName() + "/" + metaTable.getTableName() + "_index");
            MetaAttribute primaryKeyMA = metaTable.getPrimaryKey();
            bPlusTreeHashMap.put(entry.getKey(), parseBtree(BTreeFile, primaryKeyMA));
        }
        this.bPlusTreeHashMap = bPlusTreeHashMap;
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

    public File createIndexFile(String table){
        String index_path = db.getName() + "/" + table + "_index";
        File file = new File(index_path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
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
        System.out.println("Saving B+ Trees");
        persistBTrees();
    }
}
