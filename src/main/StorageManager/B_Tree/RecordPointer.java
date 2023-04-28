package main.StorageManager.B_Tree;

import main.Constants.Constant;
import main.Constants.Helper;

import java.util.Arrays;

public class RecordPointer {

    private int pageNumber;
    private int recordIndex;

    public RecordPointer(int pageNumber, int recordIndex){
        this.pageNumber = pageNumber;
        this.recordIndex = recordIndex;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getRecordIndex() {
        return recordIndex;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setRecordIndex(int recordIndex) {
        this.recordIndex = recordIndex;
    }

    public byte[] serialize() {
        byte[] pageNumberBytes = Helper.convertIntToByteArray(pageNumber);
        byte[] recordIndexBytes = Helper.convertIntToByteArray(recordIndex);
        return Helper.concatenate(pageNumberBytes, recordIndexBytes);
    }

    public static RecordPointer deserialize(byte[] bytes) {
        int index = 0;
        int pageNumber = Helper.convertByteArrayToInt(
                Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE));
        index += Constant.INTEGER_SIZE;
        int recordIndex = Helper.convertByteArrayToInt(
                Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE));
        return new RecordPointer(pageNumber, recordIndex);
    }
}
