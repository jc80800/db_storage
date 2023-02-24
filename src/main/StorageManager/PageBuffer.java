package main.StorageManager;

import static main.Constants.Constant.PrepareResult.PREPARE_SUCCESS;
import static main.Constants.Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import main.Constants.Constant;
import main.Constants.Coordinate;
import main.StorageManager.Data.Page;
import main.StorageManager.Data.Record;
import main.StorageManager.Data.TableHeader;
import main.StorageManager.MetaData.Catalog;
import main.StorageManager.MetaData.MetaTable;

public class PageBuffer {

    public final HashMap<Integer, Page> pages;
    private final int bufferSize;
    private final int pageSize;
    private final Deque<Page> bufferQueue;
    private final File db;
    private Catalog catalog;

    public PageBuffer(int bufferSize, int pageSize, File db, Catalog catalog) {
        this.pageSize = pageSize;
        this.bufferSize = bufferSize;
        this.db = db;
        this.bufferQueue = new LinkedList<>();
        this.pages = new HashMap<>();
        this.catalog = catalog;
    }

    public Page getPage(int pageId) {
        // If page already in queue, remove it and put it to front of queue
        if (pages.containsKey(pageId)) {
            bufferQueue.remove(pages.get(pageId));
            Page page = pages.get(pageId);
            bufferQueue.push(page);
            return page;
        }
        return null;
    }

    public void putPage(Page page) {
        if (bufferQueue.size() >= bufferSize) {
            Page removedPage = bufferQueue.removeLast();
            updatePage(removedPage);
            pages.remove(removedPage.getPageId());
        }
        bufferQueue.push(page);
        pages.put(page.getPageId(), page);
    }

    public void updateAllPage() {
        for (Page page : this.bufferQueue) {
            updatePage(page);
        }
    }

    public void updatePage(Page page) {
        int pageId = page.getPageId();
        int tableNumber = page.getTableNumber();

        MetaTable metaTable = this.catalog.getMetaTable(tableNumber);
        String tableName = metaTable.getTableName();
        String table_path = db.getName() + "/" + tableName;
        File file = new File(table_path);

        TableHeader tableHeader = TableHeader.parseTableHeader(file, pageSize);
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


    public Constant.PrepareResult findRecordPlacement(File table_file,
        ArrayList<Record> records, MetaTable metaTable, TableHeader tableHeader) {
        int tableNumber = tableHeader.getTableNumber();

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(table_file.getPath(), "rw");

            if (tableHeader.getCoordinates().size() == 0) {
                putPage(tableHeader.createFirstPage(this.pageSize));
            }

            ArrayList<Coordinate> coordinates = tableHeader.getCoordinates();

            for (Record record : records) {
                Boolean inserted;

                for (int i = 0; i < coordinates.size(); i++) {
                    Page page = getPage(i);
                    if (page == null) {
                        // If the page Buffer doesn't have it, go to the file and deserialize
                        byte[] bytes = new byte[this.pageSize];
                        randomAccessFile.seek(coordinates.get(i).getOffset());
                        randomAccessFile.readFully(bytes);
                        page = Page.deserialize(bytes, metaTable, tableNumber, this.pageSize, i);
                        putPage(page);
                    }

                    ArrayList<Record> pageRecords = page.getRecords();

                    // Check if record can be placed in this page
                    inserted = checkPlacement(pageRecords, record, page, tableHeader);
                    if (inserted == null) {
                        System.out.println("PK already exist");
                        return PREPARE_UNRECOGNIZED_STATEMENT;
                    } else if (inserted) {
                        break;
                    }

                    if (i == coordinates.size() - 1) {
                        // If no place, insert at the very end
                        // something
                        insertRecord(record, page, pageRecords.size(), tableHeader);
                        break;
                    }
                }
            }
            randomAccessFile.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return PREPARE_SUCCESS;

    }

    public void insertRecord(Record record, Page page, int index, TableHeader tableHeader) {
        Page potentialNewPage = page.insertRecord(record, index, tableHeader);
        if (potentialNewPage != null) {
            putPage(potentialNewPage);
        }
    }

    public Boolean checkPlacement(ArrayList<Record> records, Record target, Page page,
        TableHeader tableHeader) {
        Object recordValue = target.getPrimaryKey().getValue();

        for (int i = 0; i < records.size(); i++) {
            Record currentPageRecord = records.get(i);
            Object value = currentPageRecord.getPrimaryKey().getValue();

            if (value instanceof String) {
                if (((String) value).compareTo((String) recordValue) == 0) {
                    return null;
                }

                if (((String) value).compareTo((String) recordValue) > 0) {
                    // if record's string is less than current record
                    insertRecord(target, page, i, tableHeader);
                    return true;
                }
            } else if (value instanceof Boolean) {
                if ((boolean) value == (boolean) recordValue) {
                    return null;
                }
            } else if (value instanceof Integer) {
                if ((int) value == (int) recordValue) {
                    return null;
                }
                if ((int) value > (int) recordValue) {
                    // if record's int is less than current record
                    insertRecord(target, page, i, tableHeader);
                    return true;
                }
            } else {
                if ((double) value == (double) recordValue) {
                    return null;
                }
                if ((double) value > (double) recordValue) {
                    // record's double is less
                    insertRecord(target, page, i, tableHeader);
                    return true;
                }
            }
        }
        return false;
    }
}


