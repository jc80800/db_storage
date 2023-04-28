package main.StorageManager.B_Tree;

import main.Constants.Constant;

import java.io.File;
import java.util.ArrayList;
import main.Constants.Constant.DataType;

public class BPlusTree {
    Integer rootIndex;
    int N;
    File file;
    static int nums;
    private DataType dataType;

    // for testing purposes
    static ArrayList<Node> nodes = new ArrayList<>();

    public BPlusTree(int N, File file, DataType dataType) {
        this.N = N;
        this.file = file;
        this.rootIndex = null;
        nums = 0;
        this.dataType = dataType;

    }

    private Node getRoot() {
        Node rootNode;
        if (rootIndex == null) {
            rootNode = new Node(this.dataType, true, N, nums++, this);
            nodes.add(rootNode);
            rootIndex = rootNode.getIndex();
        } else{
            rootNode = nodes.get(rootIndex);
        }
        return rootNode;
    }

    public void updateRecordPointer(Object searchKey, int newPageId, int newRecordIndex) {
        Node root = getRoot();
        Node node = root.search(searchKey);
        ArrayList<Object> searchKeys = node.getSearchKeys();
        ArrayList<RecordPointer> recordPointers = node.getRecordPointers();
        for (int i = 0; i < searchKeys.size(); i++) {
            int compareValue = node.compareValues(searchKey, searchKeys.get(i));
            if (compareValue == 0) {
                RecordPointer recordPointer = recordPointers.get(i);
                recordPointer.setPageNumber(newPageId);
                recordPointer.setRecordIndex(newRecordIndex);
                return;
            }
        }
    }

    public Node search(Node root, Object searchKey) {
        return root.search(searchKey);
    }

    public RecordPointer findRecordPlacement(Object searchKey) {
        Node root = getRoot();
        Node nodeToInsert = search(root, searchKey);
        return nodeToInsert.findRecordPointer(nodeToInsert, searchKey);
    }

    public void insert(Object key, int pageNumber, int recordNumber) {
        Node rootNode = getRoot();
        Node nodeToInsert = search(rootNode, key);
        Node newRoot = nodeToInsert.insert(key, pageNumber, recordNumber);
        if (newRoot != null) {
            rootIndex = newRoot.getIndex();
        }
    }


    public static void putNode(Node node) {
        nodes.add(node);
    }

    public RecordPointer delete(Object key){
        if(rootIndex == null){
            System.out.println("Table is empty! Nothing to delete!");
            return null;
        }
        else{
            Node rootNode = nodes.get(rootIndex);
            return rootNode.delete(key);
        }
    }

    public void update(Object key, Object value){
//        delete(key);
//        insert(value);
    }

    public static void insertNodeForTesting(Node node){
        nodes.add(node);
    }

    public void setRootIndex(int index){
        this.rootIndex = index;
    }

    public static Node getNodeAtIndex(int index) {
       return nodes.get(index);
    }

    public static int getNextIndexAndIncrement() {
        return nums++;
    }

    public static void printNodes(){
        for(Node n : nodes){
            System.out.println(n);
        }
    }

    @Override
    public String toString() {
        if (rootIndex == null) {
            return "No tree";
        }
        Node root = nodes.get(rootIndex);
        return root.toString();
    }
}
