package main.StorageManager.B_Tree;

public class Node {

    private Object value;
    private boolean ifLeaf;
    private Node prev;
    private Node next;
    private Bucket bucket;

    public Node(Object value, boolean ifLeaf){
        this.value = value;
        this.ifLeaf = ifLeaf;
        this.prev = null;
        this.next = null;
        this.bucket = null;
    }

    public void setNext(Node node){
        this.next = node;
    }

    public void setPrev(Node node){
        this.prev = node;
    }

    public Object getValue(){
        return this.value;
    }

    public boolean getIfLeaf(){
        return this.ifLeaf;
    }

    public Node getNext(){
        return this.next;
    }

    public Node getPrev(){
        return this.prev;
    }
}
