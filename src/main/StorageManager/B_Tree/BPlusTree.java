package main.StorageManager.B_Tree;

import main.Constants.Constant;

import java.io.File;
import java.util.ArrayList;

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
            rootIndex = 0;
        } else{
            rootNode = nodes.get(rootIndex);
        }
        ArrayList<Node> result = rootNode.insert(key);
        if (result != null) {
            Node newRoot = result.get(0);
            rootIndex = newRoot.getIndex();
            if (result.size() > 1) {
                Node node = result.get(1);
                nodes.add(node);
            }
            nodes.add(newRoot);
        }
    }

    public static Node getNodeAtIndex(int index) {
       return nodes.get(index);
    }

    public static int getNextIndexAndIncrement() {
        return nums++;
    }
}
