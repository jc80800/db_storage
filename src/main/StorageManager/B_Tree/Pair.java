package main.StorageManager.B_Tree;

public class Pair {

    private int pageNumber;
    private int index;

    public Pair(int pageNumber, int index){
        this.pageNumber = pageNumber;
        this.index = index;
    }

    public int getPageNumber(){
        return this.pageNumber;
    }

    public int getIndex(){
        return this.index;
    }

}
