package main.StorageManager.Data;

import static java.lang.Math.max;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import main.Constants.Constant;
import main.Constants.Coordinate;
import main.Constants.Helper;
import main.StorageManager.MetaData.Catalog;
import main.StorageManager.MetaData.MetaTable;
import main.StorageManager.PageBuffer;

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
        updateTableHeader(index);
    }

    public Page createFirstPage(int pageSize){
        Page page = new Page(pageSize, this.tableNumber, new ArrayList<>(), 0);
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

    public void updateTableHeader(int newPageIndex){
        try {

            if(this.currentNumOfPages > this.maxPages){
                this.maxPages += 10;
                makeNewFile(this.db.getPath());
            } else {
                RandomAccessFile randomAccessFile = new RandomAccessFile(this.db.getPath(), "rw");

                randomAccessFile.seek(0);
                byte[] bytes = this.serialize();
                randomAccessFile.write(bytes);
                Coordinate newCoordinate = this.coordinates.get(newPageIndex);
                byte[] bytes1 = new byte[newCoordinate.getLength()];
                randomAccessFile.seek(newCoordinate.getOffset());
                randomAccessFile.write(bytes1);
                randomAccessFile.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeNewFile(String path) {

        // Make the temp file
        File tempFile = new File(this.db.getPath() + "/temp");
        File file = new File(path);

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(path, "rw");
            RandomAccessFile tempRandomAccessFile = new RandomAccessFile(tempFile.getPath(), "rw");
            if(tempFile.createNewFile()) {

                // Make the new file and write the new header with the coordinate adjusted
                tempRandomAccessFile.seek(0);

                updateCoordinates();
                tempRandomAccessFile.write(this.serialize());

                // find the old file's offset where the first page starts
                int offset = (4 * 4) + ((this.maxPages - 10) * Coordinate.getBinarySize());
                randomAccessFile.seek(offset);

                // copy the old pages over
                for (int i = 0; i < coordinates.size() - 1; i++){
                    byte[] bytes = new byte[coordinates.get(0).getLength()];
                    randomAccessFile.readFully(bytes);
                    tempRandomAccessFile.write(bytes);
                }

                // Write the new page padding
                byte[] bytes1 = new byte[coordinates.get(0).getLength()];
                tempRandomAccessFile.write(bytes1);

                // Rename and overwrite old file
                if(tempFile.renameTo(file)){
                    System.out.println("File renamed");
                } else {
                    System.out.println("File couldn't be renamed");
                }
            }
            randomAccessFile.close();
            tempRandomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void updateCoordinates() {
        for (Coordinate coordinate : this.coordinates){
            coordinate.padOffset(10 * Coordinate.getBinarySize());
        }

    }

    public byte[] serialize(){
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
    public String toString(){
        return "Table Number: " + this.tableNumber +
            " Page Capacity: " + this.maxPages +
            " Number Of Pages: " + this.currentNumOfPages +
            " Number of Records" + this.numRecords +
            " Coordinates: " + this.coordinates;
    }

    public int getTotalRecords(File file, PageBuffer pageBuffer, MetaTable metaTable, int pageSize) {
        int total = 0;
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file.getPath(), "rw");
            for(int i = 0; i < this.coordinates.size(); i++){
                Page page = pageBuffer.getPage(i);
                if (page == null){
                    Coordinate coordinate = this.coordinates.get(i);
                    randomAccessFile.seek(coordinate.getOffset());
                    byte[] bytes = new byte[coordinate.getLength()];
                    randomAccessFile.readFully(bytes);

                    page = Page.deserialize(bytes, metaTable, metaTable.getTableNumber(), pageSize, i);
                    total += page.getNumberOfRecords();
                } else{
                    total += page.getNumberOfRecords();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return total;
    }
}
