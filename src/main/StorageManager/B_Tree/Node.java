package main.StorageManager.B_Tree;

import java.util.ArrayList;

public class Node {

    private int size;
    private boolean isLeaf;
    private Node parentPointer;
    private ArrayList<Object> value;
    private ArrayList<Node> childPointers;
    private Bucket bucket;

    public Node(int size){
        this.size = size;
        this.isLeaf = false;
        this.value = new ArrayList<>();
        this.childPointers = new ArrayList<>();
        this.bucket = null;
        this.parentPointer = null;
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

    public void insertNode(Object value){
        // TODO create and insert Node of value
    }

    public void deleteNode(Object value){
        // TODO delete node with primarykey value
    }

    public void updateNode(){
        // TODO not sure about parameter but update Node
    }

    public void splitNode(){
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
