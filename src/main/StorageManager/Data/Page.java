package main.StorageManager.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import main.Constants.Constant;
import main.Constants.Coordinate;
import main.Constants.Helper;
import main.StorageManager.B_Tree.BPlusTree;
import main.StorageManager.MetaData.MetaTable;
import main.StorageManager.PageBuffer;

public class Page {

    private final int pageId;
    private final int pageSize;
    private final int tableNumber;
    private final int availableSpace;
    private ArrayList<Coordinate> recordPointers;
    private ArrayList<Record> records;

    public Page(int pageSize, int tableNumber,
        ArrayList<Record> records, int pageId) {
        this.pageSize = pageSize;
        this.tableNumber = tableNumber;
        this.records = records;
        this.recordPointers = constructPointers();
        this.availableSpace = calculateAvailableSpace();
        this.pageId = pageId;
    }

    public Page(int pageSize, int tableNumber, ArrayList<Coordinate> recordPointers,
        ArrayList<Record> records, int pageId) {
        this.pageSize = pageSize;
        this.tableNumber = tableNumber;
        this.recordPointers = recordPointers;
        this.records = records;
        this.availableSpace = calculateAvailableSpace();
        this.pageId = pageId;
    }

    public static Page deserialize(byte[] bytes, MetaTable metaTable, int tableNumber,
        int pageSize) {

        int index = 0;

        int pageId = Helper.convertByteArrayToInt(
            Arrays.copyOf(bytes, index += Constant.INTEGER_SIZE));

        int numOfRecords = Helper.convertByteArrayToInt(
            Arrays.copyOfRange(bytes, index, index += Constant.INTEGER_SIZE));
        ArrayList<Coordinate> pointers = new ArrayList<>();
        ArrayList<Record> records = new ArrayList<>();

        while (numOfRecords > 0) {
            Coordinate coordinate = Coordinate.deserialize(
                Arrays.copyOfRange(bytes, index, index += Coordinate.getBinarySize()));
            pointers.add(coordinate);

            Record record = Record.deserialize(Arrays.copyOfRange(bytes, coordinate.getOffset(),
                coordinate.getOffset() + coordinate.getLength()), metaTable.metaAttributes());
            records.add(record);
            numOfRecords--;
        }

        return new Page(pageSize, tableNumber, pointers, records, pageId);
    }

    public static int deserializePageId(byte[] bytes){
        int index = 0;
        return Helper.convertByteArrayToInt(
            Arrays.copyOf(bytes, index += Constant.INTEGER_SIZE));
    }

    public boolean isEnoughSpaceForInsert(Record record) {
        int available = calculateAvailableSpace();
        return available >= (record.getBinarySize() + Coordinate.getBinarySize());
    }

    /***
     * form: [numOfRecords(int), recordPointers(list of coordinate), list of records]
     * @return byte array
     */
    public byte[] serialize() {
        byte[] pageIdBytes = Helper.convertIntToByteArray(this.pageId);
        byte[] numOfRecordsBytes = Helper.convertIntToByteArray(records.size());
        byte[] pointersBytes = new byte[0];
        for (Coordinate pointer : recordPointers) {
            byte[] pointerBytes = pointer.serialize();
            pointersBytes = Helper.concatenate(pointersBytes, pointerBytes);
        }

        byte[] recordsBytes = new byte[0];
        for (Record record : records) {
            byte[] recordBytes = record.serialize();
            recordsBytes = Helper.concatenate(recordBytes, recordsBytes);
        }
        byte[] bytes = Helper.concatenate(pageIdBytes, numOfRecordsBytes, pointersBytes);

        // fill 0's between pointers and records
        bytes = Arrays.copyOf(bytes, pageSize - recordsBytes.length);

        return Helper.concatenate(bytes, recordsBytes);
    }

    private ArrayList<Coordinate> constructPointers() {
        ArrayList<Coordinate> pointers = new ArrayList<>();
        int offset = pageSize;
        for (Record record : records) {
            int metaTableBinarySize = record.getBinarySize();
            pointers.add(new Coordinate(offset -= metaTableBinarySize, metaTableBinarySize));
        }
        return pointers;
    }

    private int calculateAvailableSpace() {
        int spaceTaken = Constant.INTEGER_SIZE + Constant.INTEGER_SIZE;
        spaceTaken += Coordinate.getBinarySize() * recordPointers.size();
        for (Record record : records) {
            spaceTaken += record.getBinarySize();
        }
        return pageSize - spaceTaken;
    }

    public Page deleteRecord(Record record, int pageId, TableHeader tableHeader) {
        this.records.remove(record);
        this.recordPointers = constructPointers();
        return this;
    }

    public void deleteRecordAtIndex(int index, TableHeader tableHeader){
        this.records.remove(index);
    }

    public Page insertRecord(Record record, int index, TableHeader tableHeader, PageBuffer pageBuffer, int currentPageIndex) {
        boolean canInsert = isEnoughSpaceForInsert(record);

        this.records.add(index, record);
        if(canInsert) {
            this.recordPointers = constructPointers();
            return null;
        } else {
            // split the page's record and put into a new page
            int splittingPoint = this.records.size() / 2;
            ArrayList<Record> temp = new ArrayList<>(this.records.subList(splittingPoint, this.records.size()));
            this.records = new ArrayList<>(
                this.records.subList(0, splittingPoint));
            this.recordPointers = constructPointers();

            Page newPage = new Page(this.pageSize, this.tableNumber, temp, tableHeader.getCurrentNumOfPages());

            tableHeader.insertNewPage(currentPageIndex + 1, newPage.getPageId());
            return newPage;
        }
    }

    public int getRecordIndex(Record record) {
        for (int i = 0; i < records.size(); i++) {
            Record temp = records.get(i);
            if (record.equals(temp)) {
                return i;
            }
        }
        return -1;
    }

    public boolean containsRecord(Record record) {
        for (Record temp : records) {
            if (record.equals(temp)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Record> getRecords() {
        return records;
    }

    public int getTableNumber() {
        return this.tableNumber;
    }

    public int getPageId() {
        return this.pageId;
    }

    public int getNumberOfRecords() {
        return this.records.size();
    }

    @Override
    public String toString() {
        return "Page{" +
            "tableNumber=" + tableNumber +
            ", recordPointers=" + recordPointers +
            ", records=" + records +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Page page = (Page) o;
        return tableNumber == page.tableNumber && recordPointers.equals(page.recordPointers)
            && records.equals(page.records);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableNumber, recordPointers, records);
    }
}
