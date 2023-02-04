package StorageManager;

public class Coordinate {

    private long offset;
    private int length;

    public Coordinate(long offset, int length) {
        this.offset = offset;
        this.length = length;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
