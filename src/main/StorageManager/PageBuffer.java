package main.StorageManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import main.Constants.Coordinate;
import main.StorageManager.Data.Page;
import main.StorageManager.Data.TableHeader;
import main.StorageManager.MetaData.Catalog;
import main.StorageManager.MetaData.MetaTable;

public class PageBuffer {

    private final int bufferSize;
    private final int pageSize;
    private final Deque<Page> bufferQueue;
    private final File db;
    private Catalog catalog;

    public final HashMap<Integer, Page> pages;

    public PageBuffer(int bufferSize, int pageSize, File db, Catalog catalog) {
        this.pageSize = pageSize;
        this.bufferSize = bufferSize;
        this.db = db;
        this.bufferQueue = new LinkedList<>();
        this.pages = new HashMap<>();
        this.catalog = catalog;
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
            updatePage(removedPage);
            pages.remove(removedPage.getPageId());
        }
        bufferQueue.push(page);
        pages.put(page.getPageId(), page);
    }

    public void updateAllPage(){
        for(Page page : this.bufferQueue){
            updatePage(page);
        }
    }

    public void updatePage(Page page){
        int pageId = page.getPageId();
        int tableNumber = page.getTableNumber();

        MetaTable metaTable = this.catalog.getMetaTable(tableNumber);
        String tableName = metaTable.getTableName();
        String table_path = db.getName() + "/" + tableName;
        File file = new File(table_path);

        TableHeader tableHeader = TableHeader.parseTableHeader(file);
        ArrayList<Coordinate> coordinates = tableHeader.getCoordinates();
        Coordinate coordinate = coordinates.get(pageId);

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file.getPath(), "rw");
            randomAccessFile.seek(coordinate.getOffset());
            randomAccessFile.write(page.serialize());
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateCatalog(Catalog catalog) {
        this.catalog = catalog;
    }
}
