package main.StorageManager.B_Tree;

import java.util.ArrayList;
import main.Constants.Constant.DataType;

public class Node {

    private DataType dataType;
    private boolean isLeaf;
    private int parent;
    private ArrayList<Object> searchKeys;
    private ArrayList<RecordPointer> recordPointers;

    public Node(DataType dataType){
        this.dataType = dataType;
        this.isLeaf = true;
        this.searchKeys = new ArrayList<>();
        this.recordPointers = new ArrayList<>();
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void insert(Object searchValue){
        // TODO create and insert Node of value
        for(int i = 0; i < this.searchKeys.size(); i++){
            int compareValue = compareValues(searchValue, this.searchKeys.get(i));
            if(compareValue == 0){
                System.out.println("Primary Key already exist");
                return;
            }

            if (compareValue < 0){
                if(this.isLeaf){
                    this.searchKeys.add(i, searchValue);
                }
                else{

                }
                // TODO insert newValue right before this
            }
        }
        if(this.isLeaf){
            this.searchKeys.add(searchValue);
        }

    }

    public void delete(Object value){
        // TODO delete node with primarykey value
    }

    public void update(){
        // TODO not sure about parameter but update Node
    }

    public void split(){
        // TODO split node once it exceeds size
    }

    public void merge(){
        // TODO merge node if the size is too low
    }

    public byte[] serialize(){
        // TODO
        return null;
    }

    public static Node deserialize(byte[] bytes){
        // TODO
        return null;
    }

    public int compareValues(Object searchValue, Object compareValue){
        return switch (dataType) {
            case INTEGER -> ((Integer) searchValue).compareTo((Integer) compareValue);
            case DOUBLE -> ((Double) searchValue).compareTo((Double) compareValue);
            case BOOLEAN -> ((Boolean) searchValue).compareTo((Boolean) compareValue);
            case VARCHAR -> ((String) searchValue).compareTo((String) compareValue);
            default -> ((Character) searchValue).compareTo((Character) compareValue);
        };
    }

}
