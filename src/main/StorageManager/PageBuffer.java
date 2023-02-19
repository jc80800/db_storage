package main.StorageManager;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;

import main.StorageManager.Data.Page;

public class PageBuffer {

    private final int bufferSize;
    private final int pageSize;
    private final Deque<Page> bufferQueue;

    public final HashMap<Integer, Page> pages;

    public PageBuffer(int bufferSize, int pageSize) {
        this.pageSize = pageSize;
        this.bufferSize = bufferSize;
        this.bufferQueue = new LinkedList<>();
        this.pages = new HashMap<>();
    }

    public Page getPage(int pageId){
        // If page already in queue, remove it and put it to front of queue
        if(pages.containsKey(pageId)){
            bufferQueue.remove(pages.get(pageId));
            Page page = pages.get(pageId);
            bufferQueue.push(page);
            return page;
        }
        return null;
    }

    public void putPage(Page page){
        if(bufferSize - bufferQueue.size() * pageSize < pageSize){
            bufferQueue.removeLast();
            Page removedPage = bufferQueue.removeLast();
            pages.remove(removedPage.getPageId());
        }
        bufferQueue.push(page);
        pages.put(page.getPageId(), page);
    }

}
