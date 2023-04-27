package test.B_tree;

import main.StorageManager.B_Tree.BPlusTree;
import org.junit.jupiter.api.Test;

public class TreeTest {
    @Test
    void BTree() {
        BPlusTree bPlusTree = new BPlusTree(4, null);
        bPlusTree.insert(10);
        System.out.println(bPlusTree);
        bPlusTree.insert(9);
        System.out.println(bPlusTree);
        bPlusTree.insert(8);
        System.out.println(bPlusTree);
        bPlusTree.insert(7);
        System.out.println(bPlusTree);
        bPlusTree.insert(6);
        System.out.println(bPlusTree);
        bPlusTree.insert(5);
        System.out.println(bPlusTree);
        bPlusTree.insert(4);
        System.out.println(bPlusTree);
        bPlusTree.insert(3);
        System.out.println(bPlusTree);
        bPlusTree.insert(2);
        System.out.println(bPlusTree);
        bPlusTree.insert(1);
        System.out.println(bPlusTree);
    }
}
