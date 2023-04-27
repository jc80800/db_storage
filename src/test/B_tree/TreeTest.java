package test.B_tree;

import main.StorageManager.B_Tree.BPlusTree;
import org.junit.jupiter.api.Test;

public class TreeTest {
    @Test
    void BTree() {
        BPlusTree bPlusTree = new BPlusTree(4, null);
        bPlusTree.insert(10);
        System.out.println(bPlusTree);
        bPlusTree.insert(15);
        System.out.println(bPlusTree);
        bPlusTree.insert(20);
        System.out.println(bPlusTree);
        bPlusTree.insert(25);
        System.out.println(bPlusTree);
        bPlusTree.insert(5);
        System.out.println(bPlusTree);
        bPlusTree.insert(1);
        System.out.println(bPlusTree);
        bPlusTree.insert(11);
        System.out.println(bPlusTree);

    }
}
