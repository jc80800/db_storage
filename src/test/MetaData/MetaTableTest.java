package test.MetaData;

import static org.junit.jupiter.api.Assertions.*;

import main.Constants.Constant.DataType;
import main.StorageManager.MetaData.MetaAttribute;
import main.StorageManager.MetaData.MetaTable;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class MetaTableTest {

    @Test
    void serialization() {
        ArrayList<MetaAttribute> metaAttributes = new ArrayList<>();
        metaAttributes.add(new MetaAttribute(true, "num", DataType.INTEGER));

        MetaTable metaTable = new MetaTable(0, "foo", metaAttributes);

        byte[] bytes = metaTable.serialize();
        MetaTable deserializedTable = MetaTable.deserialize(bytes);
        assertEquals(metaTable, deserializedTable);

    }
}