package StorageManager;

import java.util.ArrayList;

public class Page {

    private final int tableNumber;
    private int numOfRecords;
    private ArrayList<Coordinate> recordPointers;
    private ArrayList<Record> records;

    public Page(int tableNumber, int numOfRecords, ArrayList<Coordinate> recordPointers,
        ArrayList<Record> records) {
        this.tableNumber = tableNumber;
        this.numOfRecords = numOfRecords;
        this.recordPointers = recordPointers;
        this.records = records;
    }

    public int getNumOfRecords() {
        return numOfRecords;
    }

    public void setNumOfRecords(int numOfRecords) {
        this.numOfRecords = numOfRecords;
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
}
