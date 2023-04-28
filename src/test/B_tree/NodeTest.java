package test.B_tree;

import main.Constants.Constant;
import main.StorageManager.B_Tree.BPlusTree;
import main.StorageManager.B_Tree.Node;
import main.StorageManager.B_Tree.RecordPointer;
import main.StorageManager.MetaData.MetaAttribute;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class NodeTest {
    @Test
    void serialization() {
        MetaAttribute metaAttribute1 = new MetaAttribute(true, "id", Constant.DataType.INTEGER,
                null);
        BPlusTree bPlusTree = new BPlusTree(null, metaAttribute1, 100);
        RecordPointer recordPointer = new RecordPointer(1, 1);
        ArrayList<RecordPointer> recordPointers = new ArrayList<>();
        recordPointers.add(recordPointer);
        ArrayList<Object> searchKeys = new ArrayList<>();
        searchKeys.add(1);
        int N = 2;
        Node node = new Node(metaAttribute1, false, searchKeys, recordPointers, N, -1, 0, bPlusTree);
        byte[] bytes = node.serialize();
        Node deserialized = Node.deserialize(bytes, metaAttribute1, N, bPlusTree);
        assertEquals(node, deserialized);
    }
}