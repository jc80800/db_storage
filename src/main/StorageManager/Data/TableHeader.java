package main.StorageManager.Data;

import static java.lang.Math.max;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import main.Constants.Constant;
import main.Constants.Coordinate;
import main.Constants.Helper;

public class TableHeader {

    private int tableNumber;
    private int pageSize;
    private int currentPageSize;
    private int numRecords;
    private ArrayList<Coordinate> coordinates;

    public TableHeader(int tableNumber, int pageSize, int currentPageSize, int numRecords, ArrayList<Coordinate> coordinates){
        this.tableNumber = tableNumber;
        this.pageSize = pageSize;
        this.currentPageSize = currentPageSize;
        this.numRecords = numRecords;
        this.coordinates = coordinates;
    }

    public static TableHeader parseTableHeader(File table_file) {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(table_file.getPath(), "rw");

            // Get table #
            byte[] tableNumberByte = new byte[4];
            randomAccessFile.read(tableNumberByte);
            int tableNumber = Helper.convertByteArrayToInt(tableNumberByte);

            // current page capacity
            byte[] pageCapacityBytes = new byte[4];
            randomAccessFile.read(pageCapacityBytes);
            int pageCapacity = Helper.convertByteArrayToInt(pageCapacityBytes);

            // get current number of pages in table
            byte[] numPagesBytes = new byte[4];
            randomAccessFile.read(numPagesBytes);
            int numPages = Helper.convertByteArrayToInt(numPagesBytes);

            //get number of records in table
            byte[] recordBytes = new byte[4];
            randomAccessFile.read(recordBytes);
            int numRecords = Helper.convertByteArrayToInt(recordBytes);

            // Calculate the size of the entire coordinate header
            int totalCoordinateSize = numPages * 8;

            // Read in the coordinate
            byte[] coordinateBytes = new byte[totalCoordinateSize];
            randomAccessFile.readFully(coordinateBytes);
            ArrayList<Coordinate> coordinates = Coordinate.deserializeList(coordinateBytes);

            return new TableHeader(tableNumber, pageCapacity, numPages, numRecords, coordinates);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void insertNewPage(Page page, int index){
        int lastOffset = findLastOffset();
        Coordinate coordinate = new Coordinate(lastOffset + this.pageSize, this.pageSize);
        this.coordinates.add(index, coordinate);
    }

    public void createFirstPage(){
        Page page = new Page(this.pageSize, this.tableNumber, new ArrayList<Record>(), 1);
        insertNewPage(page, 0);
    }

    public int getTableNumber() {
        return this.tableNumber;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public int getCurrentPageSize() {
        return this.currentPageSize;
    }

    public int getNumRecords() {
        return this.numRecords;
    }

    public ArrayList<Coordinate> getCoordinates() {
        return this.coordinates;
    }

    public int getBinarySize(){
        return (Constant.INTEGER_SIZE * 4) + (8 * coordinates.size());
    }

    public int findLastOffset(){
        int result = 16; // first offset after all the 4 other integers in the header
        for(Coordinate coordinate : this.coordinates){
            result = max(coordinate.getOffset(), result);
        }
        return result;
    }


}
