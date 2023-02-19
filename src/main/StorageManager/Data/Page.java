package main.StorageManager.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import main.Constants.Constant;
import main.Constants.Coordinate;
import main.Constants.Helper;
import main.StorageManager.MetaData.MetaTable;

public class Page {

    private final int pageId;
    private final int pageSize;
    private final int tableNumber;
    private ArrayList<Coordinate> recordPointers;
    private ArrayList<Record> records;
    private final int availableSpace;

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

    public boolean isEnoughSpaceForInsert(Record record) {
        return availableSpace >= (record.getBinarySize() + Coordinate.getBinarySize());
    }

    public static Page deserialize(byte[] bytes, MetaTable metaTable, int tableNumber,
        int pageSize, int pageId) {
        int index = 0;
        int numOfRecords = Helper.convertByteArrayToInt(
            Arrays.copyOf(bytes, index += Constant.INTEGER_SIZE));
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

    /***
     * form: [numOfRecords(int), recordPointers(list of coordinate), list of records]
     * @return byte array
     */
    public byte[] serialize() {
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
        byte[] bytes = Helper.concatenate(numOfRecordsBytes, pointersBytes);

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
        int spaceTaken = Constant.INTEGER_SIZE;
        spaceTaken += Coordinate.getBinarySize() * recordPointers.size();
        for (Record record : records) {
            spaceTaken += record.getBinarySize();
        }
        return pageSize - spaceTaken;
    }

    public int getNumOfRecords() {
        return records.size();
    }

    public ArrayList<Coordinate> getRecordPointers() {
        return recordPointers;
    }

    public void setRecordPointers(ArrayList<Coordinate> recordPointers) {
        this.recordPointers = recordPointers;
    }

    public ArrayList<Record> getRecords() {
        return records;
    }

    public void setRecords(ArrayList<Record> records) {
        this.records = records;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public int getPageId(){
        return this.pageId;
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
