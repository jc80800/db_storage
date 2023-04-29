package test.B_tree;

import main.Constants.Constant;
import main.StorageManager.B_Tree.BPlusTree;
import main.StorageManager.B_Tree.Node;
import main.StorageManager.MetaData.MetaAttribute;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BPlusTreeTest {
    @Test
    void serialization() {
        MetaAttribute metaAttribute = new MetaAttribute(true, "num", Constant.DataType.INTEGER,
                null);
        int pageSize = 100;
        BPlusTree bPlusTree = new BPlusTree(null, metaAttribute, pageSize);
        byte[] bytes = bPlusTree.serialize();
        BPlusTree deserialized = BPlusTree.deserialize(bytes, null, metaAttribute, pageSize);
        assertEquals(bPlusTree,deserialized);
    }

    @Test
    void testDelete(){
        MetaAttribute metaAttribute = new MetaAttribute(true, "num", Constant.DataType.INTEGER,
                null);
        BPlusTree bPlusTree = new BPlusTree(null, metaAttribute, 100);

        Node root = new Node(metaAttribute, false, 9, 0, bPlusTree);
        root.insertValuesForTesting(5);

        root.setRecordPointers(1);
        root.setRecordPointers(2);

        Node node1 = new Node(metaAttribute, true, 9, 1, bPlusTree);
        node1.insertValuesForTesting(1);
        node1.insertValuesForTesting(2);
        node1.insertValuesForTesting(3);
        node1.insertValuesForTesting(4);
        node1.setParentIndexForTesting(0);

        node1.setRecordPointers(255);
        node1.setRecordPointers(255);
        node1.setRecordPointers(255);
        node1.setRecordPointers(255);

        Node node2 = new Node(metaAttribute, true, 9, 2, bPlusTree);
        node2.insertValuesForTesting(5);
        node2.insertValuesForTesting(6);
        node2.insertValuesForTesting(7);
        node2.insertValuesForTesting(8);
        node2.setParentIndexForTesting(0);

        node2.setRecordPointers(255);
        node2.setRecordPointers(255);
        node2.setRecordPointers(255);
        node2.setRecordPointers(255);

        bPlusTree.setRootIndex(0);

        System.out.println(bPlusTree);
        bPlusTree.delete(5);
        System.out.println(bPlusTree);
    }
}