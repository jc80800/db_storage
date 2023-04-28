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

        System.out.println(bPlusTree);
        bPlusTree.delete(3);
        System.out.println(bPlusTree);

    }

    @Test
    void testBorrowRight(){
        BPlusTree bPlusTree = new BPlusTree(4, null);
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
        BPlusTree bPlusTree = new BPlusTree(4, null);
        Node root = new Node(Constant.DataType.INTEGER, false, 4, 0);
        root.insertValuesForTesting(3);
        root.insertValuesForTesting(6);
        root.insertValuesForTesting(8);


        Node node1 = new Node(Constant.DataType.INTEGER, true, 4, 1);
        node1.insertValuesForTesting(1);
        node1.insertValuesForTesting(2);
        node1.setParentIndexForTesting(0);

        Node node2 = new Node(Constant.DataType.INTEGER, true, 4, 2);
        node2.insertValuesForTesting(3);
        node2.insertValuesForTesting(4);
        node2.insertValuesForTesting(5);
        node2.setParentIndexForTesting(0);

        Node node3 = new Node(Constant.DataType.INTEGER, true, 4, 3);
        node3.insertValuesForTesting(6);
        node3.insertValuesForTesting(7);
        node3.setParentIndexForTesting(0);

        Node node4 = new Node(Constant.DataType.INTEGER, true, 4, 4);
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
    void testBorrowInternalLeftNode(){
        BPlusTree bPlusTree = new BPlusTree(4, null);

        Node root = new Node(Constant.DataType.INTEGER, false, 4, 0);
        root.insertValuesForTesting(22);

        root.setRecordPointers(1);
        root.setRecordPointers(5);

        Node node1 = new Node(Constant.DataType.INTEGER, false, 4, 1);
        node1.insertValuesForTesting(16);
        node1.insertValuesForTesting(18);
        node1.setParentIndexForTesting(0);

        node1.setRecordPointers(2);
        node1.setRecordPointers(3);
        node1.setRecordPointers(4);

        Node CNode1 = new Node(Constant.DataType.INTEGER, true, 4, 2);
        CNode1.insertValuesForTesting(14);
        CNode1.insertValuesForTesting(15);
        CNode1.setParentIndexForTesting(1);

        CNode1.setRecordPointers(5123);
        CNode1.setRecordPointers(3123);

        Node CNode2 = new Node(Constant.DataType.INTEGER, true, 4, 3);
        CNode2.insertValuesForTesting(16);
        CNode2.insertValuesForTesting(17);
        CNode2.setParentIndexForTesting(1);

        CNode2.setRecordPointers(3123);
        CNode2.setRecordPointers(3123);

        Node CNode3 = new Node(Constant.DataType.INTEGER, true, 4, 4);
        CNode3.insertValuesForTesting(18);
        CNode3.insertValuesForTesting(21);
        CNode3.setParentIndexForTesting(1);

        CNode3.setRecordPointers(3123);
        CNode3.setRecordPointers(3123);

        // Trying to borrow
        Node node2 = new Node(Constant.DataType.INTEGER, false, 4, 5);
        node2.insertValuesForTesting(24);
        node2.insertValuesForTesting(26);
        node2.insertValuesForTesting(28);
        node2.setParentIndexForTesting(0);

        node2.setRecordPointers(6);
        node2.setRecordPointers(7);
        node2.setRecordPointers(8);
        node2.setRecordPointers(9);

        Node CNode11 = new Node(Constant.DataType.INTEGER, true, 4, 6);
        CNode11.insertValuesForTesting(22);
        CNode11.insertValuesForTesting(23);
        CNode11.setParentIndexForTesting(5);

        CNode11.setRecordPointers(3123);
        CNode11.setRecordPointers(3123);

        Node CNode22 = new Node(Constant.DataType.INTEGER, true, 4, 7);
        CNode22.insertValuesForTesting(24);
        CNode22.insertValuesForTesting(25);
        CNode22.setParentIndexForTesting(5);

        CNode22.setRecordPointers(3123);
        CNode22.setRecordPointers(3123);

        Node CNode33 = new Node(Constant.DataType.INTEGER, true, 4, 8);
        CNode33.insertValuesForTesting(26);
        CNode33.insertValuesForTesting(27);
        CNode33.setParentIndexForTesting(5);

        CNode33.setRecordPointers(3123);
        CNode33.setRecordPointers(3123);

        Node CNode44 = new Node(Constant.DataType.INTEGER, true, 4, 9);
        CNode44.insertValuesForTesting(28);
        CNode44.insertValuesForTesting(29);
        CNode44.insertValuesForTesting(31);
        CNode44.setParentIndexForTesting(5);

        CNode44.setRecordPointers(3123);
        CNode44.setRecordPointers(3123);
        CNode44.setRecordPointers(3123);

        bPlusTree.setRootIndex(0);
        BPlusTree.insertNodeForTesting(root);
        BPlusTree.insertNodeForTesting(node1);
        BPlusTree.insertNodeForTesting(CNode1);
        BPlusTree.insertNodeForTesting(CNode2);
        BPlusTree.insertNodeForTesting(CNode3);
        BPlusTree.insertNodeForTesting(node2);
        BPlusTree.insertNodeForTesting(CNode11);
        BPlusTree.insertNodeForTesting(CNode22);
        BPlusTree.insertNodeForTesting(CNode33);
        BPlusTree.insertNodeForTesting(CNode44);

        System.out.println(bPlusTree);
        bPlusTree.delete(21);
        System.out.println(bPlusTree);
    }

    @Test
    void testRootDeletion(){
        BPlusTree bPlusTree = new BPlusTree(3, null);

        Node root = new Node(Constant.DataType.INTEGER, false, 4, 0, bPlusTree);
        root.insertValuesForTesting(2);

        root.setRecordPointers(1);
        root.setRecordPointers(2);

        Node node1 = new Node(Constant.DataType.INTEGER, true, 4, 1);
        node1.insertValuesForTesting(1);
        node1.setParentIndexForTesting(0);

        node1.setRecordPointers(255);

        Node node2 = new Node(Constant.DataType.INTEGER, true, 4, 2);
        node2.insertValuesForTesting(3);
        node2.setParentIndexForTesting(0);

        node2.setRecordPointers(255);

        bPlusTree.setRootIndex(0);
        BPlusTree.insertNodeForTesting(root);
        BPlusTree.insertNodeForTesting(node1);
        BPlusTree.insertNodeForTesting(node2);

        System.out.println(bPlusTree);
        bPlusTree.delete(1);
        System.out.println(bPlusTree);

    }
}
