package main.StorageManager.B_Tree;

public class Pair {

    private int pageNumber;

    // TODO what's an index???
    public Pair(int pageNumber){
        this.pageNumber = pageNumber;
    }

    public int getPageNumber(){
        return this.pageNumber;
    }

}
