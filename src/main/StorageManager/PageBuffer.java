package main.StorageManager;

import java.io.File;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import main.StorageManager.Data.Page;
import main.StorageManager.Data.TableHeader;

public class PageBuffer {

    private final int bufferSize;
    private final int pageSize;
    private final Deque<Page> bufferQueue;
    private final File db;

    public final HashMap<Integer, Page> pages;

    public PageBuffer(int bufferSize, int pageSize, File db) {
        this.pageSize = pageSize;
        this.bufferSize = bufferSize;
        this.db = db;
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
            Page removedPage = bufferQueue.removeLast();
            StorageManager.updateFile(removedPage);
            pages.remove(removedPage.getPageId());
        }
        bufferQueue.push(page);
        pages.put(page.getPageId(), page);
    }

}
