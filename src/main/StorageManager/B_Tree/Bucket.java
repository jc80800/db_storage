package main.StorageManager.B_Tree;

import java.util.ArrayList;

public class Bucket {

    private int pageNumber;
    private int index;
    private Record record;

    public Bucket(int pageNumber, int index, Record record){
        this.pageNumber = pageNumber;
        this.index = index;
        this.record = record;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getIndex() {
        return index;
    }

    public Record getRecord() {
        return record;
    }

}
