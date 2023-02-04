package StorageManager.Metadata;

import StorageManager.Metadata.Attribute.MetaAttribute;
import java.util.ArrayList;

public record Metatable(int tableNumber, String tableName,
                        ArrayList<MetaAttribute> metaAttributes) {

}
