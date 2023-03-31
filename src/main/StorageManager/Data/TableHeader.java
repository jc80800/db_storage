package main.StorageManager.Data;

import static java.lang.Math.max;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import main.Constants.Constant;
import main.Constants.Coordinate;
import main.Constants.Helper;
import main.StorageManager.MetaData.MetaTable;
import main.StorageManager.PageBuffer;

public class TableHeader {

    private final int pageSize;
    private int tableNumber;
    private int maxPages;
    private int currentNumOfPages;
    private int numRecords;
    private ArrayList<Coordinate> coordinates;
    private File table_file;


    public TableHeader(int tableNumber, int maxPages, int currentNumOfPages, int numRecords,
        ArrayList<Coordinate> coordinates, File table_file, int pageSize) {
        this.tableNumber = tableNumber;
        this.maxPages = maxPages;
        this.currentNumOfPages = currentNumOfPages;
        this.numRecords = numRecords;
        this.coordinates = coordinates;
        this.table_file = table_file;
        this.pageSize = pageSize;
    }

    public TableHeader(int tableNumber, File table_file, int pageSize) {
        this.tableNumber = tableNumber;
        this.maxPages = Constant.INITIAL_POINTER_SIZE;
        this.currentNumOfPages = 0;
        this.numRecords = 0;
        this.coordinates = new ArrayList<>();
        this.table_file = table_file;
        this.pageSize = pageSize;
    }

    public static TableHeader parseTableHeader(File table_file, int pageSize) {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(table_file.getPath(), "rw");

            // Get table #
            byte[] tableNumberByte = new byte[Constant.INTEGER_SIZE];
            randomAccessFile.read(tableNumberByte);
            int tableNumber = Helper.convertByteArrayToInt(tableNumberByte);

            // current page capacity
            byte[] pageCapacityBytes = new byte[Constant.INTEGER_SIZE];
            randomAccessFile.read(pageCapacityBytes);
            int pageCapacity = Helper.convertByteArrayToInt(pageCapacityBytes);

            // get current number of pages in table
            byte[] numPagesBytes = new byte[Constant.INTEGER_SIZE];
            randomAccessFile.read(numPagesBytes);
            int numPages = Helper.convertByteArrayToInt(numPagesBytes);

            //get number of records in table
            byte[] recordBytes = new byte[Constant.INTEGER_SIZE];
            randomAccessFile.read(recordBytes);
            int numRecords = Helper.convertByteArrayToInt(recordBytes);

            // Calculate the size of the entire coordinate header
            int totalCoordinateSize = numPages * 8;

            // Read in the coordinate
            byte[] coordinateBytes = new byte[totalCoordinateSize];
            randomAccessFile.readFully(coordinateBytes);
            ArrayList<Coordinate> coordinates = Coordinate.deserializeList(coordinateBytes);

            return new TableHeader(tableNumber, pageCapacity, numPages, numRecords, coordinates,
                table_file, pageSize);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void insertNewPage(int index) {
        int lastOffset = findLastOffset();
        if (this.coordinates.size() > 0) {
            lastOffset += this.pageSize;
        }
        Coordinate coordinate = new Coordinate(lastOffset, this.pageSize);
        if (index < this.coordinates.size()) {
            this.coordinates.add(index, coordinate);
        } else {
            this.coordinates.add(coordinate);
        }
        this.currentNumOfPages += 1;
        updateTableHeader(index);
    }

    // only deletes page coordinate, page will be left on disk and not be used
    public void deletePage(int pageId) {
        coordinates.remove(pageId);
        currentNumOfPages -= 1;
        updateTableHeader(pageId);
    }

    public Page createFirstPage(int pageSize) {
        Page page = new Page(pageSize, this.tableNumber, new ArrayList<>(), 0);
        insertNewPage(0);
        return page;
    }

    public int getTableNumber() {
        return this.tableNumber;
    }

    public ArrayList<Coordinate> getCoordinates() {
        return this.coordinates;
    }

    public int getBinarySize() {
        return (Constant.INTEGER_SIZE * 4) + (Coordinate.getBinarySize() * coordinates.size());
    }

    public int findLastOffset() {
        int result =
            Constant.INTEGER_SIZE * 4 + maxPages
                * Coordinate.getBinarySize(); // first offset after all the 4 other integers in the header
        for (Coordinate coordinate : this.coordinates) {
            result = max(coordinate.getOffset(), result);
        }
        return result;
    }

    public void updateTableHeader(int newPageIndex) {
        try {
            if (this.currentNumOfPages > this.maxPages) {
                this.maxPages += Constant.INITIAL_POINTER_SIZE;
                makeNewFile(this.table_file.getPath());
            } else {
                RandomAccessFile randomAccessFile = new RandomAccessFile(this.table_file.getPath(),
                    "rw");

                randomAccessFile.seek(0);
                byte[] bytes = this.serialize();
                randomAccessFile.write(bytes);
//                Coordinate newCoordinate = this.coordinates.get(newPageIndex);
//                byte[] bytes1 = new byte[newCoordinate.getLength()];
//                randomAccessFile.seek(newCoordinate.getOffset());
//                randomAccessFile.write(bytes1);
                randomAccessFile.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeNewFile(String path) {

        // Make the temp file
        File tempFile = new File("./temp");
        File file = new File(path);

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(path, "rw");
            if (tempFile.createNewFile()) {

                RandomAccessFile tempRandomAccessFile = new RandomAccessFile(tempFile.getPath(),
                    "rw");

                // Make the new file and write the new header with the coordinate adjusted
                tempRandomAccessFile.seek(0);

                updateCoordinates();
                tempRandomAccessFile.write(this.serialize());

                // find the old file's offset where the first page starts
                int offset =
                    (Constant.INTEGER_SIZE * 4) + ((this.maxPages - Constant.INITIAL_POINTER_SIZE)
                        * Coordinate.getBinarySize());
                randomAccessFile.seek(offset);

                // copy the old pages over
                for (int i = 0; i < coordinates.size() - 1; i++) {
                    byte[] bytes = new byte[coordinates.get(0).getLength()];
                    randomAccessFile.readFully(bytes);
                    tempRandomAccessFile.write(bytes);
                }

                // Write the new page padding
                byte[] bytes1 = new byte[coordinates.get(0).getLength()];
                tempRandomAccessFile.write(bytes1);

                // Rename and overwrite old file
                tempFile.renameTo(file);
                tempRandomAccessFile.close();
            }
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void updateCoordinates() {
        for (Coordinate coordinate : this.coordinates) {
            coordinate.padOffset(Constant.INITIAL_POINTER_SIZE * Coordinate.getBinarySize());
        }

    }

    public byte[] serialize() {
        byte[] tableNumberByte = Helper.convertIntToByteArray(this.tableNumber);
        byte[] pageCapacityByte = Helper.convertIntToByteArray(this.maxPages);
        byte[] currentPageCapacityByte = Helper.convertIntToByteArray(this.currentNumOfPages);
        byte[] recordBytes = Helper.convertIntToByteArray(this.numRecords);
        byte[] coordinateBytes = Coordinate.serializeList(this.coordinates);
        int pagesLeft = this.maxPages - this.currentNumOfPages;
        byte[] paddingByte = new byte[pagesLeft * Coordinate.getBinarySize()];

        return Helper.concatenate(tableNumberByte, pageCapacityByte, currentPageCapacityByte,
            recordBytes, coordinateBytes, paddingByte);
    }

    @Override
    public String toString() {
        return "Table Number: " + this.tableNumber +
            " Page Capacity: " + this.maxPages +
            " Number Of Pages: " + this.currentNumOfPages +
            " Number of Records" + this.numRecords +
            " Coordinates: " + this.coordinates;
    }

    public int getTotalRecords(File file, PageBuffer pageBuffer, MetaTable metaTable,
        int pageSize) {
        int total = 0;
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file.getPath(), "rw");
            for (int i = 0; i < this.coordinates.size(); i++) {
                Page page = pageBuffer.getPage(i, this);
                if (page == null) {
                    Coordinate coordinate = this.coordinates.get(i);
                    randomAccessFile.seek(coordinate.getOffset());
                    byte[] bytes = new byte[pageSize];
                    randomAccessFile.readFully(bytes);

                    page = Page.deserialize(bytes, metaTable, metaTable.getTableNumber(), pageSize,
                        i);
                }
                total += page.getNumberOfRecords();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return total;
    }

    public File getTable_file() {
        return table_file;
    }
}
