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
    private File db;

    public TableHeader(int tableNumber, int pageSize, int currentPageSize, int numRecords, ArrayList<Coordinate> coordinates, File db){
        this.tableNumber = tableNumber;
        this.pageSize = pageSize;
        this.currentPageSize = currentPageSize;
        this.numRecords = numRecords;
        this.coordinates = coordinates;
        this.db = db;
    }

    public TableHeader(int tableNumber, File db){
        this.tableNumber = tableNumber;
        this.pageSize = 10;
        this.currentPageSize = 0;
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
            lastOffset += this.pageSize;
        }
        Coordinate coordinate = new Coordinate(lastOffset, this.pageSize);
        if(index < this.coordinates.size()){
            this.coordinates.add(index, coordinate);
        } else {
            this.coordinates.add(coordinate);
        }
        this.currentPageSize += 1;
    }

    public Page createFirstPage(){
        Page page = new Page(this.pageSize, this.tableNumber, new ArrayList<>(), 0);
        insertNewPage(page, 0);
        this.currentPageSize += 1;
        return page;
    }

    public int getTableNumber() {
        return this.tableNumber;
    }

    public ArrayList<Coordinate> getCoordinates() {
        return this.coordinates;
    }

    public int getBinarySize(){
        return (Constant.INTEGER_SIZE * 4) + (8 * coordinates.size());
    }

    public int findLastOffset(){
        int result = 16 + (this.pageSize * 8); // first offset after all the 4 other integers in the header
        for(Coordinate coordinate : this.coordinates){
            result = max(coordinate.getOffset(), result);
        }
        return result;
    }

    public void updateTableHeader(){
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(this.db.getPath(), "rw");

            if(this.currentPageSize > this.pageSize){
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
        byte[] bytes = new byte[0];

        byte[] tableNumberByte = Helper.convertIntToByteArray(this.tableNumber);
        byte[] pageCapacityByte = Helper.convertIntToByteArray(this.pageSize);
        byte[] currentPageCapacityByte = Helper.convertIntToByteArray(this.currentPageSize);
        byte[] recordBytes = Helper.convertIntToByteArray(this.numRecords);
        byte[] coordinateBytes = Coordinate.serializeList(this.coordinates);

        bytes = Helper.concatenate(bytes, tableNumberByte);
        bytes = Helper.concatenate(bytes, pageCapacityByte);
        bytes = Helper.concatenate(bytes, currentPageCapacityByte);
        bytes = Helper.concatenate(bytes, recordBytes);
        bytes = Helper.concatenate(bytes, coordinateBytes);

        return bytes;
    }


}
