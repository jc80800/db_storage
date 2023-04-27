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
        ArrayList<Node> result = rootNode.insert(key);
        if (result != null) {
            result.sort(Comparator.comparingInt(Node::getIndex));
            for (Node node : result) {
                if (node.isRoot()) {
                    rootIndex = node.getIndex();
                }
                nodes.add(node);
            }
        }
    }

    public static Node getNodeAtIndex(int index) {
       return nodes.get(index);
    }

    public static int getNextIndexAndIncrement() {
        return nums++;
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
