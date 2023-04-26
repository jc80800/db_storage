package main.StorageManager.B_Tree;

import java.util.ArrayList;

public class Node {

    private int size;
    private boolean isLeaf;
    private ArrayList<Object> value;
    private ArrayList<Node> childPointers;
    private Bucket bucket;

    public Node(int size){
        this.size = size;
        this.isLeaf = false;
        this.value = new ArrayList<>();
        this.childPointers = new ArrayList<>();
        this.bucket = null;
    }

    public int getSize() {
        return size;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public ArrayList<Object> getValue() {
        return value;
    }

    public ArrayList<Node> getChildPointers() {
        return childPointers;
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
