package main.StorageManager.B_Tree;

import main.Constants.Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public class BPlusTree {
    Integer rootIndex;
    int N;
    File file;
    static int nums;

    // for testing purposes
    static ArrayList<Node> nodes = new ArrayList<>();

    public BPlusTree(int N, File file) {
        this.N = N;
        this.file = file;
        this.rootIndex = null;
        nums = 0;
    }

    public void insert(int key) {
        Node rootNode;
        if (rootIndex == null) {
            rootNode = new Node(Constant.DataType.INTEGER, true, N, nums++);
            nodes.add(rootNode);
            rootIndex = rootNode.getIndex();
        } else{
            rootNode = nodes.get(rootIndex);
        }
        Node newRoot = rootNode.insert(key);
        if (newRoot != null) {
            rootIndex = newRoot.getIndex();
        }
    }

    public static void putNode(Node node) {
        nodes.add(node);
    }

    public void delete(int key){
        if(rootIndex == null){
            System.out.println("Table is empty! Nothing to delete!");
        }
        else{
            Node rootNode = nodes.get(rootIndex);
            rootNode.delete(key);
        }
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
