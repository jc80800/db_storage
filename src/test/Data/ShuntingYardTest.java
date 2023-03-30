package test.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import main.Constants.Constant.DataType;
import main.SqlParser.ShuntingYardAlgorithm;
import main.StorageManager.Data.Attribute;
import main.StorageManager.Data.Record;
import main.StorageManager.MetaData.MetaAttribute;
import org.junit.jupiter.api.Test;

public class ShuntingYardTest {

    @Test
    void testAlgo() {
        Set<String> constraints = new HashSet<>();
        constraints.add("notnull");
        constraints.add("unique");

        MetaAttribute metaAttribute1 = new MetaAttribute(false, "FirstName", DataType.CHAR, 10,
            constraints);
        Attribute attribute1 = new Attribute(metaAttribute1, "Eldon");
        MetaAttribute metaAttribute2 = new MetaAttribute(true, "id", DataType.INTEGER, constraints);
        Attribute attribute2 = new Attribute(metaAttribute2, 1);

        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(attribute1);
        attributes.add(attribute2);

        ArrayList<MetaAttribute> metaAttributes = new ArrayList<>();
        metaAttributes.add(metaAttribute1);
        metaAttributes.add(metaAttribute2);
        Record record1 = new Record(attributes, metaAttributes);

        Queue<String> output = ShuntingYardAlgorithm.parse("id = 1 and FirstName = Eldon");
        boolean result = ShuntingYardAlgorithm.evaluate(output, record1);
        System.out.println(result);
    }

}
