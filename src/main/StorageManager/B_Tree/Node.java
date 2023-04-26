package main.StorageManager.B_Tree;

import java.util.ArrayList;

import main.Constants.Constant.DataType;

public class Node {

    private DataType dataType;
    private boolean isLeaf;
    private Integer parentIndex;
    private int index;
    private ArrayList<Object> searchKeys;
    private ArrayList<RecordPointer> recordPointers;
    private final int N;

    public Node(DataType dataType, boolean isLeaf, int N, int index){
        this.dataType = dataType;
        this.isLeaf = isLeaf;
        this.searchKeys = new ArrayList<>();
        this.recordPointers = new ArrayList<>();
        this.N = N;
        this.parentIndex = null;
        this.index = index;
    }

    public Node(DataType dataType, boolean isLeaf, ArrayList<Object> searchKeys,
                ArrayList<RecordPointer> recordPointers, int n, int index) {
        this.dataType = dataType;
        this.isLeaf = isLeaf;
        this.searchKeys = searchKeys;
        this.recordPointers = recordPointers;
        N = n;
        this.parentIndex = null;
        this.index = index;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    private ArrayList<Node> splitRoot() {
        Node sibling = this.split();
        Node parent = new Node(dataType, false, N, BPlusTree.getNextIndexAndIncrement());
        RecordPointer left = new RecordPointer(-1, this.index);
        RecordPointer right = new RecordPointer(-1, sibling.index);
        parent.addElementByIndex(0, sibling.searchKeys.get(0), left, right);
        this.parentIndex = parent.index;
        sibling.parentIndex = parent.index;
        ArrayList<Node> result = new ArrayList<>();
        result.add(parent);
        result.add(sibling);
        return result;
    }

    private Node split() {
        // TODO split node once it exceeds size
        int mid = searchKeys.size() / 2;
        // construct new Node
        ArrayList<Object> newSearchKeys = new ArrayList<>(searchKeys.subList(mid, searchKeys.size()));
        ArrayList<RecordPointer> newRecordPointers = new ArrayList<>
                (recordPointers.subList(mid, searchKeys.size()));
        Node newNode = new Node(dataType, true, newSearchKeys, newRecordPointers, N,
                BPlusTree.getNextIndexAndIncrement());

        this.searchKeys = new ArrayList<>(searchKeys.subList(0, mid));
        this.recordPointers = new ArrayList<>(recordPointers.subList(0, mid));
        return newNode;
    }

    private void addElementByIndex(int index, Object searchKey, RecordPointer left) {
        this.searchKeys.add(index, searchKey);
        this.recordPointers.add(index, left);
    }
    private void addElementByIndex(int index, Object searchKey, RecordPointer left, RecordPointer right) {
        this.searchKeys.add(index, searchKey);
        this.recordPointers.add(index, left);
        this.recordPointers.add(index + 1, right);
    }

    private ArrayList<Node> addNodeToParent(Node newNode) {
        Object searchKey = newNode.searchKeys.get(0);
        Node parent = BPlusTree.getNodeAtIndex(parentIndex);
        boolean added = false;
        for (int i = 0; i < parent.searchKeys.size(); i++) {
            int compareValue = compareValues(searchKey, this.searchKeys.get(i));
            if (compareValue < 0) {
                addElementByIndex(i, searchKey, new RecordPointer(-1, newNode.index));
                added = true;
                break;
            }
        }
        if (!added) {
            addElementByIndex(0, searchKey, new RecordPointer(-1, newNode.index));
        }
        if (parent.overflow()) {
            // if it's a root
            if (parent.parentIndex == null) {
                return splitRoot();
            }
            Node sibling = split();
            addNodeToParent(sibling);
        }
        return null;
    }

    public ArrayList<Node> insert(Object searchValue){
        // TODO create and insert Node of value
        for (int i = 0; i < this.searchKeys.size(); i++) {
            int compareValue = compareValues(searchValue, this.searchKeys.get(i));
            if (compareValue == 0) {
                // TODO might need to check if value in internal node is outdated
                System.out.println("Primary Key already exist");
                return null;
            }
            if (compareValue < 0) {
                // if it's root
                if (parentIndex == null) {
                    addElementByIndex(i, searchValue, new RecordPointer(0, (int) searchValue));
                    if (overflow()) {
                        return splitRoot();
                    }
                } else {
                    addElementByIndex(i, searchValue, new RecordPointer(0, (int) searchValue));
                    if (overflow()) {
                        Node newNode = split();
                        return addNodeToParent(newNode);
                    }
                }
                return null;
            }
        }
        // largest value
        // if it's root
        if (parentIndex == null) {
            addElementByIndex(searchKeys.size(), searchValue, new RecordPointer(0, (int) searchValue));
            if (overflow()) {
                return splitRoot();
            }
        } else {
            addElementByIndex(searchKeys.size(), searchValue, new RecordPointer(0, (int) searchValue));
            if (overflow()) {
                Node newNode = split();
                return addNodeToParent(newNode);
            }
        }
        return null;
    }

    private boolean overflow() {
        return this.searchKeys.size() == maxNum();
    }

    public void delete(Object value){
        // TODO delete node with primarykey value
    }

    public void update(){
        // TODO not sure about parameter but update Node
    }

    public void merge(){
        // TODO merge node if the size is too low
    }

    public byte[] serialize(){
        // TODO
        return null;
    }

    private int maxNum() {
        if (isLeaf) {
            return N - 1;
        } else {
            return N;
        }
    }

    private int minNum() {
        if (isLeaf) {
            return (int) Math.ceil((double) (N - 1) / 2);
        }
        return (int) Math.ceil((double) N / 2);
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

    public int getIndex() {
        return index;
    }

    public ArrayList<RecordPointer> getRecordPointers() {
        return recordPointers;
    }
}
