package test.Data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import main.Constants.Constant.DataType;
import main.StorageManager.Data.Attribute;
import main.StorageManager.Data.Record;
import main.StorageManager.MetaData.MetaAttribute;
import org.junit.jupiter.api.Test;

class RecordTest {

    @Test
    void serialization() {
        Set<String> constraints = new HashSet<>();
        constraints.add("notnull");
        constraints.add("unique");

        MetaAttribute metaAttribute1 = new MetaAttribute(false, "FirstName", DataType.CHAR, 10, constraints);
        Attribute attribute1 = new Attribute(metaAttribute1, "Eldon");
        MetaAttribute metaAttribute2 = new MetaAttribute(true, "id", DataType.INTEGER, constraints);
        Attribute attribute2 = new Attribute(metaAttribute2, 100);
        MetaAttribute metaAttribute3 = new MetaAttribute(false, "LastName", DataType.VARCHAR, 20, constraints);
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

    @Test
    void serializationWithNullValue() {
        Set<String> constraints = new HashSet<>();
        constraints.add("unique");

        MetaAttribute metaAttribute0 = new MetaAttribute(true, "Num", DataType.INTEGER, constraints);
        Attribute attribute0 = new Attribute(metaAttribute0, 1);

        MetaAttribute metaAttribute1 = new MetaAttribute(false, "LastName", DataType.VARCHAR, 20, constraints);
        Attribute attribute1 = new Attribute(metaAttribute1, null);

        ArrayList<MetaAttribute> metaAttributes = new ArrayList<>();
        metaAttributes.add(metaAttribute0);
        metaAttributes.add(metaAttribute1);

        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(attribute0);
        attributes.add(attribute1);

        Record record1 = new Record(attributes, metaAttributes);

        byte[] bytes = record1.serialize();
        Record deserialized = Record.deserialize(bytes, metaAttributes);

        assertEquals(record1, deserialized);
    }
}