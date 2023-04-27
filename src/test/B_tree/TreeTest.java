package test.B_tree;

import main.Constants.Constant;
import main.StorageManager.B_Tree.BPlusTree;
import main.StorageManager.B_Tree.Node;
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

    @Test
    void delete(){
        BPlusTree bPlusTree = new BPlusTree(3, null);
        Node root = new Node(Constant.DataType.INTEGER, false, 3, 0);
        root.insertValuesForTesting(2);
        root.insertValuesForTesting(3);

        Node node1 = new Node(Constant.DataType.INTEGER, true, 3, 1);
        node1.insertValuesForTesting(1);
        node1.setParentIndexForTesting(0);

        Node node2 = new Node(Constant.DataType.INTEGER, true, 3, 2);
        node2.insertValuesForTesting(2);
        node2.setParentIndexForTesting(0);

        Node node3 = new Node(Constant.DataType.INTEGER, true, 3, 3);
        node3.insertValuesForTesting(3);
        node3.insertValuesForTesting(4);
        node3.setParentIndexForTesting(0);

        bPlusTree.setRootIndex(0);
        BPlusTree.insertNodeForTesting(root);
        BPlusTree.insertNodeForTesting(node1);
        BPlusTree.insertNodeForTesting(node2);
        BPlusTree.insertNodeForTesting(node3);

        root.setRecordPointers(1);
        root.setRecordPointers(2);
        root.setRecordPointers(3);

        node1.setRecordPointers(1421);
        node2.setRecordPointers(231);
        node3.setRecordPointers(21321);

        //BPlusTree.printNodes();
        bPlusTree.delete(3);
        System.out.println(bPlusTree);

    }

    @Test
    void testBorrowRight(){
        BPlusTree bPlusTree = new BPlusTree(3, null);
        Node root = new Node(Constant.DataType.INTEGER, false, 4, 0);
        root.insertValuesForTesting(3);
        root.insertValuesForTesting(5);
        root.insertValuesForTesting(7);


        Node node1 = new Node(Constant.DataType.INTEGER, true, 4, 1);
        node1.insertValuesForTesting(1);
        node1.insertValuesForTesting(2);
        node1.setParentIndexForTesting(0);

        Node node2 = new Node(Constant.DataType.INTEGER, true, 4, 2);
        node2.insertValuesForTesting(3);
        node2.insertValuesForTesting(4);
        node2.setParentIndexForTesting(0);

        Node node3 = new Node(Constant.DataType.INTEGER, true, 4, 3);
        node3.insertValuesForTesting(5);
        node3.insertValuesForTesting(6);
        node3.setParentIndexForTesting(0);

        Node node4 = new Node(Constant.DataType.INTEGER, true, 4, 4);
        node4.insertValuesForTesting(7);
        node4.insertValuesForTesting(8);
        node4.insertValuesForTesting(9);
        node4.setParentIndexForTesting(0);

        bPlusTree.setRootIndex(0);
        BPlusTree.insertNodeForTesting(root);
        BPlusTree.insertNodeForTesting(node1);
        BPlusTree.insertNodeForTesting(node2);
        BPlusTree.insertNodeForTesting(node3);
        BPlusTree.insertNodeForTesting(node4);

        root.setRecordPointers(1);
        root.setRecordPointers(2);
        root.setRecordPointers(3);
        root.setRecordPointers(4);

        node1.setRecordPointers(1421);
        node1.setRecordPointers(1422);
        node2.setRecordPointers(231);
        node2.setRecordPointers(232);
        node3.setRecordPointers(21321);
        node3.setRecordPointers(2121);
        node4.setRecordPointers(21322);
        node4.setRecordPointers(21323);
        node4.setRecordPointers(21325);

        System.out.println(bPlusTree);
        bPlusTree.delete(6);
        System.out.println(bPlusTree);
    }

    @Test
    void testBorrowLeft(){
        BPlusTree bPlusTree = new BPlusTree(3, null);
        Node root = new Node(Constant.DataType.INTEGER, false, 4, 0);
        root.insertValuesForTesting(6);


        Node node1 = new Node(Constant.DataType.INTEGER, true, 4, 1);
        node1.insertValuesForTesting(1);
        node1.insertValuesForTesting(2);
        node1.insertValuesForTesting(5);
        node1.setParentIndexForTesting(0);

        Node node2 = new Node(Constant.DataType.INTEGER, true, 4, 2);
        node2.insertValuesForTesting(6);
        node2.insertValuesForTesting(7);
        node2.setParentIndexForTesting(0);

        bPlusTree.setRootIndex(0);
        BPlusTree.insertNodeForTesting(root);
        BPlusTree.insertNodeForTesting(node1);
        BPlusTree.insertNodeForTesting(node2);


        root.setRecordPointers(1);
        root.setRecordPointers(2);


        node1.setRecordPointers(1421);
        node1.setRecordPointers(1422);
        node1.setRecordPointers(1423);
        node2.setRecordPointers(231);
        node2.setRecordPointers(232);


        System.out.println(bPlusTree);
        bPlusTree.delete(6);
        System.out.println(bPlusTree);
    }
}
