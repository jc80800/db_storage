package test.MetaData;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import main.Constants.Constant.DataType;
import main.StorageManager.MetaData.Catalog;
import main.StorageManager.MetaData.MetaAttribute;
import main.StorageManager.MetaData.MetaTable;
import org.junit.jupiter.api.Test;

class CatalogTest {

    @Test
    void serialization() {
        ArrayList<MetaAttribute> metaAttributes = new ArrayList<>();
        metaAttributes.add(new MetaAttribute(true, "num", DataType.INTEGER));
        MetaTable metaTable = new MetaTable("foo", metaAttributes);

        ArrayList<MetaAttribute> metaAttributes2 = new ArrayList<>();
        metaAttributes2.add(new MetaAttribute(false, "FirstName", DataType.VARCHAR, 20));
        MetaTable metaTable2 = new MetaTable("foo", metaAttributes2);

        HashMap<Integer, MetaTable> map = new HashMap<>();
        map.put(1, metaTable);
        map.put(2, metaTable2);
        Catalog catalog = new Catalog(1024, map);

        byte[] bytes = catalog.serialize();
        Catalog deserialized = Catalog.deserialize(bytes);
        assertEquals(catalog, deserialized);
    }
}