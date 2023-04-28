package main.StorageManager;

import static main.Constants.Constant.PrepareResult.PREPARE_SUCCESS;
import static main.Constants.Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import main.Constants.Constant;
import main.Constants.Coordinate;
import main.StorageManager.B_Tree.BPlusTree;
import main.StorageManager.B_Tree.RecordPointer;
import main.StorageManager.Data.Attribute;
import main.StorageManager.Data.Page;
import main.StorageManager.Data.Record;
import main.StorageManager.Data.TableHeader;
import main.StorageManager.MetaData.Catalog;
import main.StorageManager.MetaData.MetaAttribute;
import main.StorageManager.MetaData.MetaTable;

public class PageBuffer {

    private final HashMap<PageKey, Page> pages;
    private final int bufferSize;
    private final int pageSize;
    private final Deque<Page> bufferQueue;
    private final File db;
    private final Catalog catalog;
    private Boolean index;

    public PageBuffer(int bufferSize, int pageSize, File db, Catalog catalog, Boolean index) {
        this.pageSize = pageSize;
        this.bufferSize = bufferSize;
        this.db = db;
        this.bufferQueue = new LinkedList<>();
        this.pages = new HashMap<>();
        this.catalog = catalog;
        this.index = index;
    }

    public Page getPageByPageId(int pageId, TableHeader tableHeader, ArrayList<Coordinate> coordinates) {
        PageKey pageKey = new PageKey(pageId, tableHeader.getTableNumber());
        // If page already in queue, remove it and put it to front of queue
        if (pages.containsKey(pageKey)) {
            bufferQueue.remove(pages.get(pageKey));
            Page page = pages.get(pageKey);
            bufferQueue.push(page);
            return page;
        }

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(
            tableHeader.getTable_file().getPath(), "r");) {

            for (int i = 0; i < coordinates.size(); i++){
                Coordinate coordinate = coordinates.get(i);
                randomAccessFile.seek(coordinate.getOffset());
                byte[] pageIdBytes = new byte[Constant.INTEGER_SIZE];
                randomAccessFile.readFully(pageIdBytes);

                if(pageId == Page.deserializePageId(pageIdBytes)){
                    randomAccessFile.seek(coordinate.getOffset());
                    byte[] pageBytes = new byte[catalog.getPageSize()];
                    randomAccessFile.readFully(pageBytes);

                    Page page = Page.deserialize(pageBytes,
                        catalog.getMetaTable(tableHeader.getTableNumber()),
                        tableHeader.getTableNumber(), catalog.getPageSize());
                    putPage(page);
                    return page;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }
        return null;
    }

    public Page getPage(Coordinate coordinate, TableHeader tableHeader) {

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(
            tableHeader.getTable_file().getPath(), "r");) {

            int pageId = coordinate.getLength();

            PageKey pageKey = new PageKey(pageId, tableHeader.getTableNumber());
            // If page already in queue, remove it and put it to front of queue
            if (pages.containsKey(pageKey)) {
                bufferQueue.remove(pages.get(pageKey));
                Page page = pages.get(pageKey);
                bufferQueue.push(page);
                return page;
            }

            randomAccessFile.seek(coordinate.getOffset());
            byte[] pageBytes = new byte[catalog.getPageSize()];
            randomAccessFile.readFully(pageBytes);

            Page page = Page.deserialize(pageBytes,
                catalog.getMetaTable(tableHeader.getTableNumber()),
                tableHeader.getTableNumber(), catalog.getPageSize());

            putPage(page);
            return page;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }
    }

    public void putPage(Page page) {
        if (bufferQueue.size() >= bufferSize) {
            Page removedPage = bufferQueue.removeLast();
            updatePage(removedPage);
            pages.remove(new PageKey(removedPage.getPageId(), removedPage.getTableNumber()));
        }

        bufferQueue.push(page);
        pages.put(new PageKey(page.getPageId(), page.getTableNumber()), page);
    }

    public void updateAllPage() {
        while (!this.bufferQueue.isEmpty()){
            Page page = this.bufferQueue.poll();
            updatePage(page);
        }

    }

    public void updatePage(Page page) {
        int pageId = page.getPageId();
        int tableNumber = page.getTableNumber();

        MetaTable metaTable = this.catalog.getMetaTable(tableNumber);
        String tableName = metaTable.getTableName();
        String table_path = db.getName() + "/" + tableName;
        File file = new File(table_path);

        TableHeader tableHeader = TableHeader.parseTableHeader(file, pageSize);
        ArrayList<Coordinate> coordinates = Objects.requireNonNull(tableHeader).getCoordinates();
        Coordinate coordinate = null;
        for(Coordinate coordinate1 : coordinates){
            if(coordinate1.getLength() == pageId){
                coordinate = coordinate1;
            }
        }
        assert coordinate != null;

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file.getPath(), "rw");
            randomAccessFile.seek(coordinate.getOffset());
            randomAccessFile.write(page.serialize());
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean validateRecord(Record record, Record oldRecord, MetaTable metaTable,
        TableHeader tableHeader) {

        for (int i = 0; i < metaTable.metaAttributes().size(); i++) {
            MetaAttribute metaAttribute = metaTable.metaAttributes().get(i);
            Set<String> constraints = metaAttribute.getConstraints();
            Object value = record.getAttributes().get(i).getValue();

            if (metaAttribute.getIsPrimaryKey()) {
                if (value == null) {
                    System.out.printf("Invalid value, %s is primaryKey which can not be null\n",
                        metaAttribute.getName());
                    return false;
                }
                if (oldRecord != null) {
                    if (!value.equals(oldRecord.getPrimaryKey().getValue()) && !checkUnique(value,
                        tableHeader, i)) {
                        System.out.println("Duplicate primaryKey");
                        return false;
                    }
                } else {
                    if (!checkUnique(value, tableHeader, i)) {
                        System.out.println("Duplicate primaryKey");
                        return false;
                    }
                }
            }

            if (constraints.contains("notnull") && value == null) {
                System.out.println("Invalid value: value can't be null for this column");
                return false;
            }
            boolean unique = constraints.contains("unique");
            if (unique) {
                if (oldRecord != null) {
                    if (!value.equals(oldRecord.getAttributes().get(i).getValue()) && !checkUnique(value,
                        tableHeader, i)) {
                        System.out.println("Invalid value: value is unique and already exist");
                        return false;
                    }
                } else {
                    if (!checkUnique(value, tableHeader, i)) {
                        System.out.println("Invalid value: value is unique and already exist");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void updateRecordPointersInBTree(BPlusTree bPlusTree, Page page) {
        for (int i = 0; i < page.getNumberOfRecords(); i++) {
            Record record = page.getRecords().get(i);
            bPlusTree.updateRecordPointer(record.getPrimaryKey().getValue(), page.getPageId(), i);
        }
    }

    public Constant.PrepareResult findRecordPlacement(ArrayList<Record> records, TableHeader tableHeader, BPlusTree bPlusTree) {
        if (tableHeader.getCoordinates().size() == 0) {
            putPage(tableHeader.createFirstPage(this.pageSize));
        }
        ArrayList<Coordinate> coordinates = tableHeader.getCoordinates();
        for (Record record : records) {
            if (index) {
                Object searchKey = record.getPrimaryKey().getValue();
                RecordPointer recordPointer = bPlusTree.findRecordPlacement(searchKey);
                // first record entry
                if (recordPointer == null) {
                    int pageNumber = 0;
                    int recordIndex = 0;
                    Page page = getPageByPageId(pageNumber, tableHeader, tableHeader.getCoordinates());

                    insertRecord(record, page, recordIndex, tableHeader, -1);
                    bPlusTree.insert(searchKey, page.getPageId(), page.getRecordIndex(record));
                    continue;
                }
                int pageNumber = recordPointer.getPageNumber();
                int recordIndex = recordPointer.getRecordIndex();
                Page page = getPageByPageId(pageNumber, tableHeader, tableHeader.getCoordinates());
                Constant.DataType type = record.getPrimaryKey().getMetaAttribute().getType();
                int compareResult = compareValues(type, searchKey, page.getRecords().get(recordIndex).getPrimaryKey().getValue());
                if (compareResult > 0) {
                    recordIndex++;
                } else if (compareResult == 0) {
                    // duplicate primary key
                    return PREPARE_UNRECOGNIZED_STATEMENT;
                }
                // TODO change -1
                Page newPage = insertRecord(record, page, recordIndex, tableHeader, -1);
                int pageId = page.containsRecord(record) ? page.getPageId() : newPage.getPageId();
                recordIndex = page.getRecordIndex(record) != -1
                        ? page.getRecordIndex(record)
                        : newPage.getRecordIndex(record);
                bPlusTree.insert(searchKey, pageId, recordIndex);
                updateRecordPointersInBTree(bPlusTree, page);
                if (newPage != null) {
                    updateRecordPointersInBTree(bPlusTree, newPage);
                }
            } else {

                for (int i = 0; i < coordinates.size(); i++) {

                    Page page = getPage(coordinates.get(i), tableHeader);
                    ArrayList<Record> pageRecords = page.getRecords();

                    // Check if record can be placed in this page
                    try {
                        if (checkPlacement(pageRecords, record, page, tableHeader, i)) {
                            return PREPARE_SUCCESS;
                        }
                    } catch (IllegalArgumentException e) {
                        return PREPARE_UNRECOGNIZED_STATEMENT;
                    }
                    if (i == coordinates.size() - 1) {
                        // If no place, insert at the very end
                        insertRecord(record, page, pageRecords.size(), tableHeader, i);
                        break;
                    }
                }
            }
        }
        return PREPARE_SUCCESS;

    }

    public Page insertRecord(Record record, Page page, int index, TableHeader tableHeader, int currentPageIndex) {
        Page potentialNewPage = page.insertRecord(record, index, tableHeader, this, currentPageIndex);
        if (potentialNewPage != null) {
            putPage(potentialNewPage);
        }

        return potentialNewPage;
    }

    public void deleteRecord(Record record, Page page, int index, TableHeader tableHeader) {
        Page temp = page.deleteRecord(record, index, tableHeader);
        if (temp == null) {
            deletePage(page, tableHeader.getTableNumber());
        }
    }

    public Boolean checkPlacement(ArrayList<Record> records, Record target, Page page,
        TableHeader tableHeader, int pageIndex) {
        Object recordValue = target.getPrimaryKey().getValue();

        System.out.println("Checking the placement of the record on page " + pageIndex);

        for (int i = 0; i < records.size(); i++) {
            Constant.DataType type = target.getPrimaryKey().getMetaAttribute().getType();
            Record currentPageRecord = records.get(i);
            Object value = currentPageRecord.getPrimaryKey().getValue();
            int compareResult = compareValues(type, recordValue, value);
            if (compareResult == 0) {
                System.out.printf("Duplicate primary key %s\n",
                        recordValue);
                throw new IllegalArgumentException();
            }
            if (compareResult < 0) {
                System.out.println("Inserting the Record at index " + i + "on pageIndex of " + pageIndex);
                insertRecord(target, page, i, tableHeader, pageIndex);
                return true;
            }
        }
        return false;
    }
    private int compareValues(Constant.DataType dataType, Object searchValue, Object compareValue) {
        return switch (dataType) {
            case INTEGER -> ((Integer) searchValue).compareTo((Integer) compareValue);
            case DOUBLE -> ((Double) searchValue).compareTo((Double) compareValue);
            case BOOLEAN -> ((Boolean) searchValue).compareTo((Boolean) compareValue);
            case VARCHAR -> ((String) searchValue).compareTo((String) compareValue);
            default -> ((Character) searchValue).compareTo((Character) compareValue);
        };
    }

    /**
     * Check if pages that belong to table that's to be dropped Remove pages from buffer queue and
     * hash map (pages)
     *
     * @param tableNumber table number for table to be dropped
     */
    public void deletePagesFromTable(int tableNumber) {
        for (Page page : bufferQueue) {
            if (page.getTableNumber() == tableNumber) {
                deletePage(page, tableNumber);
            }
        }
    }

    public void deletePage(Page page, int tableNumber) {
        bufferQueue.remove(page);
        PageKey pageKey = new PageKey(page.getPageId(), tableNumber);
        pages.remove(pageKey);
    }

    public ArrayList<String[]> copyRecords(File table_file, MetaAttribute metaAttribute,
        Object defaultValue, String action, MetaTable metaTable) {
        TableHeader tableHeader = TableHeader.parseTableHeader(table_file, pageSize);
        int tableNumber = tableHeader.getTableNumber();
        ArrayList<Coordinate> coordinates = tableHeader.getCoordinates();
        ArrayList<String[]> result = new ArrayList<>();

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(table_file, "r");
            for (int i = 0; i < coordinates.size(); i++) {
                Page page = getPage(coordinates.get(i), tableHeader);

                ArrayList<Record> pageRecords = page.getRecords();
                for (Record record : pageRecords) {
                    StringBuilder value = new StringBuilder();
                    ArrayList<MetaAttribute> metaAttributes = record.getMetaAttributes();
                    ArrayList<Attribute> attributes = record.getAttributes();
                    int idx = -1;
                    if (action.equals(Constant.DROP)) {
                        idx = metaAttributes.indexOf(metaAttribute);
                    }
                    for (int j = 0; j < metaAttributes.size(); j++) {
                        if (j == idx) {
                            continue;
                        }
                        if (attributes.get(j).getValue() == null) {
                            value.append("null ");
                        } else {
                            if (metaAttributes.get(j).getType().equals(Constant.DataType.VARCHAR) ||
                                metaAttributes.get(j).getType().equals(Constant.DataType.CHAR)) {
                                value.append("\"");
                                value.append(attributes.get(j).getValue().toString());
                                value.append("\"").append(" ");
                                ;
                            } else {
                                value.append(attributes.get(j).getValue().toString()).append(" ");
                            }
                        }
                    }
                    if (action.equals(Constant.ADD)) {
                        value.append(defaultValue);
                    }
                    String[] temp = new String[]{value.toString().trim()};
                    result.add(temp);
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;

    }

    public boolean checkUnique(Object value, TableHeader tableHeader, int index) {
        ArrayList<Coordinate> coordinates = tableHeader.getCoordinates();
        for (int i = 0; i < coordinates.size(); i++) {
            Page page = getPage(coordinates.get(i), tableHeader);
            ArrayList<Record> pageRecords = page.getRecords();
            for (Record record : pageRecords) {
                if ((record.getAttributes().get(index).getValue()).equals(value)
                    || record.getAttributes().get(index).getValue() == value) {
                    System.out.println("Exists");
                    return false;
                }
            }
        }

        return true;
    }

    private class PageKey {

        private int pageId;
        private int tableNumber;

        private PageKey(int pageId, int tableNumber) {
            this.pageId = pageId;
            this.tableNumber = tableNumber;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof PageKey) {
                return this.pageId == ((PageKey) other).pageId
                    && this.tableNumber == ((((PageKey) other).tableNumber));
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return (int) (Math.pow(3, tableNumber) + Math.pow(2, pageId));
        }
    }
}


