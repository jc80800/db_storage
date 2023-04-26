package main.StorageManager.B_Tree;

public class Pair {

    private int pageNumber;
    private int index;
    private Record record;

    public Pair(int pageNumber, int index, Record record){
        this.pageNumber = pageNumber;
        this.index = index;
        this.record = record;
    }

    public int getPageNumber(){
        return pageNumber;
    }

    public int getIndex(){
        return index;
    }

    public Record getRecord(){
        return record;
    }

}
