package main.StorageManager.B_Tree;

public class RecordPointer {

    private int pageNumber;
    private int index;

    public RecordPointer(int pageNumber, int index){
        this.pageNumber = pageNumber;
        this.index = index;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getIndex() {
        return index;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
