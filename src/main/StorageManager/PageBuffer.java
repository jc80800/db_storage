package main.StorageManager;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;

import main.StorageManager.Data.Page;

public class PageBuffer {

    private final int bufferSize;
    private final int pageSize;
    private final Deque<Page> bufferQueue;

    public PageBuffer(int bufferSize, int pageSize) {
        this.pageSize = pageSize;
        this.bufferSize = bufferSize;
        this.bufferQueue = new LinkedList<>();
    }

    public void putPage(Page page){
        if(bufferSize - bufferQueue.size() * pageSize < pageSize){
            bufferQueue.removeLast();
        }
        bufferQueue.push(page);
    }

    public HashSet<Page> getPages(int tableNumber){
        HashSet<Page> pages = new HashSet<>();
        for(Page page:bufferQueue){
            if(page.getTableNumber() == tableNumber){
                pages.add(page);
                bufferQueue.remove(page);
                bufferQueue.add(page);
            }
        }
        return pages;
    }

}
