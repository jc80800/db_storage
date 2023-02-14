package test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import main.Constants.Constant.DataType;
import main.StorageManager.Data.Attribute;
import main.StorageManager.Data.Record;
import main.StorageManager.Metadata.MetaAttribute;
import org.junit.jupiter.api.Test;

class RecordTest {

    @Test
    void serialization() {
        MetaAttribute metaAttribute1 = new MetaAttribute(false, "FirstName", DataType.CHAR, 10);
        Attribute attribute1 = new Attribute(metaAttribute1, "Eldon");
        MetaAttribute metaAttribute2 = new MetaAttribute(true, "id", DataType.INTEGER);
        Attribute attribute2 = new Attribute(metaAttribute2, 100);
        MetaAttribute metaAttribute3 = new MetaAttribute(false, "LastName", DataType.VARCHAR, 20);
        Attribute attribute3 = new Attribute(metaAttribute3, "Lin");
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(attribute1);
        attributes.add(attribute2);
        attributes.add(attribute3);

        ArrayList<MetaAttribute> metaAttributes = new ArrayList<>();
        metaAttributes.add(metaAttribute1);
        metaAttributes.add(metaAttribute2);
        metaAttributes.add(metaAttribute3);

        Record record1 = new Record(attributes, metaAttributes);

        byte[] bytes = record1.serialize();
        Record deserialized = Record.deserialize(bytes, metaAttributes);

        assertEquals(record1, deserialized);
    }
}