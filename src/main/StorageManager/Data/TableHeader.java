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
    private int maxPages;
    private int currentNumOfPages;
    private int numRecords;
    private ArrayList<Coordinate> coordinates;
    private File db;

    public TableHeader(int tableNumber, int maxPages, int currentNumOfPages, int numRecords, ArrayList<Coordinate> coordinates, File db){
        this.tableNumber = tableNumber;
        this.maxPages = maxPages;
        this.currentNumOfPages = currentNumOfPages;
        this.numRecords = numRecords;
        this.coordinates = coordinates;
        this.db = db;
    }

    public TableHeader(int tableNumber, File db){
        this.tableNumber = tableNumber;
        this.maxPages = 10;
        this.currentNumOfPages = 0;
        this.numRecords = 0;
        this.coordinates = new ArrayList<>();
        this.db = db;
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

            return new TableHeader(tableNumber, pageCapacity, numPages, numRecords, coordinates, table_file);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void insertNewPage(Page page, int index){
        int lastOffset = findLastOffset();
        if(this.coordinates.size() > 0){
            lastOffset += this.maxPages;
        }
        Coordinate coordinate = new Coordinate(lastOffset, this.maxPages);
        if(index < this.coordinates.size()){
            this.coordinates.add(index, coordinate);
        } else {
            this.coordinates.add(coordinate);
        }
        this.currentNumOfPages += 1;
    }

    public Page createFirstPage(){
        Page page = new Page(this.maxPages, this.tableNumber, new ArrayList<>(), 0);
        insertNewPage(page, 0);
        this.currentNumOfPages += 1;
        return page;
    }

    public int getTableNumber() {
        return this.tableNumber;
    }

    public ArrayList<Coordinate> getCoordinates() {
        return this.coordinates;
    }

    public int getBinarySize(){
        return (Constant.INTEGER_SIZE * 4) + (Coordinate.getBinarySize() * coordinates.size());
    }

    public int findLastOffset(){
        int result = 16 + (this.maxPages * 8); // first offset after all the 4 other integers in the header
        for(Coordinate coordinate : this.coordinates){
            result = max(coordinate.getOffset(), result);
        }
        return result;
    }

    public void updateTableHeader(){
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(this.db.getPath(), "rw");

            if(this.currentNumOfPages > this.maxPages){
                // TODO split the file, make a new file and write everything over
            } else {
                randomAccessFile.seek(0);
                randomAccessFile.write(this.serialize());
            }


            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] serialize(){
        byte[] tableNumberByte = Helper.convertIntToByteArray(this.tableNumber);
        byte[] pageCapacityByte = Helper.convertIntToByteArray(this.maxPages);
        byte[] currentPageCapacityByte = Helper.convertIntToByteArray(this.currentNumOfPages);
        byte[] recordBytes = Helper.convertIntToByteArray(this.numRecords);
        byte[] coordinateBytes = Coordinate.serializeList(this.coordinates);

        return Helper.concatenate(tableNumberByte, pageCapacityByte, currentPageCapacityByte,
            recordBytes, coordinateBytes);
    }


}
