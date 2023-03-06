package test.Data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import main.Constants.Constant.DataType;
import main.StorageManager.Data.Attribute;
import main.StorageManager.MetaData.MetaAttribute;
import org.junit.jupiter.api.Test;

class AttributeTest {

    @Test
    void serialization() {
        Set<String> constraints = new HashSet<>();
        constraints.add("notnull");
        constraints.add("unique");

        MetaAttribute metaAttribute1 = new MetaAttribute(false, "FirstName", DataType.CHAR, 10, constraints);
        Attribute attribute1 = new Attribute(metaAttribute1, "Eldon");

        byte[] bytes = attribute1.serialize();
        Attribute deserialized = Attribute.deserialize(bytes, metaAttribute1);

        assertEquals(attribute1, deserialized);
    }
}