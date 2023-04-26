package main.StorageManager.B_Tree;

import java.util.ArrayList;

public class Node {
    private boolean isLeaf;
    private int parent;
    private ArrayList<Object> searchKeys;
    private ArrayList<RecordPointer> recordPointers;


    public boolean isLeaf() {
        return isLeaf;
    }

    public void insert(Object value){
        // TODO create and insert Node of value
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

}
