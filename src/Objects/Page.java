package Objects;

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
}
