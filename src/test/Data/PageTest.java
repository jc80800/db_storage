package test.Data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import main.Constants.Constant.DataType;
import main.StorageManager.Data.Attribute;
import main.StorageManager.Data.Page;
import main.StorageManager.Data.Record;
import main.StorageManager.MetaData.MetaAttribute;
import main.StorageManager.MetaData.MetaTable;
import org.junit.jupiter.api.Test;

class PageTest {

    @Test
    void serialization() {
        Set<String> constraints = new HashSet<>();
        constraints.add("notnull");
        constraints.add("unique");

        MetaAttribute metaAttribute1 = new MetaAttribute(true, "id", DataType.INTEGER, constraints);
        Attribute attribute1 = new Attribute(metaAttribute1, 100);
        MetaAttribute metaAttribute2 = new MetaAttribute(false, "FirstName", DataType.CHAR, 10, constraints);
        Attribute attribute2 = new Attribute(metaAttribute2, "Eldon");
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

        ArrayList<Record> records = new ArrayList<>();
        records.add(record1);

        MetaTable metaTable = new MetaTable(0, "Student", metaAttributes);
        Page page = new Page(1024,1, records, 0);

        byte[] bytes = page.serialize();
        Page deserialized = Page.deserialize(bytes, metaTable, 1, 1024, 1);
        assertEquals(page, deserialized);
    }
}