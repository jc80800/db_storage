package main.StorageManager.B_Tree;

import java.io.File;
import java.util.ArrayList;

public class BPlusTree {
    int root;
    int n;
    File file;

    // for testing purposes
    ArrayList<Node> nodes;

    public BPlusTree(int n, File file) {
        this.n = n;
        this.file = file;
        this.nodes = new ArrayList<>();
        this.root = -1;
    }

    public void insert(int key) {
        // TODO
    }
}
