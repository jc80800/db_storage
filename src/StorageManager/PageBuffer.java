package StorageManager;

import java.util.PriorityQueue;

public class PageBuffer {

    private final int bufferSize;
    private final PriorityQueue<Page> bufferQueue;

    public PageBuffer(int bufferSize, PriorityQueue<Page> bufferQueue) {
        this.bufferSize = bufferSize;
        this.bufferQueue = bufferQueue;
    }
}
