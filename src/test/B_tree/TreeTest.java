package test.B_tree;

import main.StorageManager.B_Tree.BPlusTree;
import org.junit.jupiter.api.Test;

public class TreeTest {
    @Test
    void BTree() {
        BPlusTree bPlusTree = new BPlusTree(3, null);
        bPlusTree.insert(3);
        bPlusTree.insert(1);
        bPlusTree.insert(2);
        bPlusTree.insert(4);
        System.out.println(bPlusTree);
    }
}
