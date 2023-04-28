package test.B_tree;

import main.StorageManager.B_Tree.RecordPointer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecordPointerTest {
    @Test
    void serialization() {
        RecordPointer recordPointer = new RecordPointer(1, 1);
        byte[] bytes = recordPointer.serialize();
        RecordPointer deserialized = RecordPointer.deserialize(bytes);
        assertEquals(recordPointer, deserialized);
    }

}