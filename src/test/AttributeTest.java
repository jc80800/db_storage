package test;

import static org.junit.jupiter.api.Assertions.*;

import main.Constants.Constant.DataType;
import main.StorageManager.Data.Attribute;
import main.StorageManager.Metadata.MetaAttribute;
import org.junit.jupiter.api.Test;

class AttributeTest {

    @Test
    void serialization() {

        MetaAttribute metaAttribute1 = new MetaAttribute(false, "FirstName", DataType.CHAR, 10);
        Attribute attribute1 = new Attribute(metaAttribute1, "Eldon");

        byte[] bytes = attribute1.serialize();
        Attribute deserialized = Attribute.deserialize(bytes, metaAttribute1);

        assertEquals(attribute1, deserialized);
    }
}