package StorageManager;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class Page {
    private int numOfRecords;
    private ArrayList<Integer> recordPointers;
    private ArrayDeque<Record> records;

    public Page(int numOfRecords, ArrayList<Integer> recordPointers, ArrayDeque<Record> records) {
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

    public ArrayList<Integer> getRecordPointers() {
        return recordPointers;
    }

    public void setRecordPointers(ArrayList<Integer> recordPointers) {
        this.recordPointers = recordPointers;
    }

    public ArrayDeque<Record> getRecords() {
        return records;
    }

    public void setRecords(ArrayDeque<Record> records) {
        this.records = records;
    }
}
