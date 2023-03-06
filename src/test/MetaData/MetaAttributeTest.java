package test.MetaData;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import main.Constants.Constant.DataType;
import main.StorageManager.MetaData.MetaAttribute;
import org.junit.jupiter.api.Test;

class MetaAttributeTest {


    @Test
    void serialization() {
        Set<String> constraints = new HashSet<>();
        constraints.add("notnull");
        constraints.add("unique");


        MetaAttribute metaAttribute1 = new MetaAttribute(true, "num", DataType.INTEGER, constraints);
        MetaAttribute metaAttribute2 = new MetaAttribute(false, "Firstname", DataType.VARCHAR, 10, constraints);

        byte[] bytes1 = metaAttribute1.serialize();
        MetaAttribute deserialized1 = MetaAttribute.deserialize(bytes1);
        assertEquals(metaAttribute1, deserialized1);

        byte[] bytes2 = metaAttribute2.serialize();
        MetaAttribute deserialize2 = MetaAttribute.deserialize(bytes2);
        assertEquals(metaAttribute2, deserialize2);
    }
}