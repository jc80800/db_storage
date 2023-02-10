package StorageManager.Metadata.Attribute;

import Constants.Constant;
import Constants.Constant.DataType;

public interface MetaAttribute {

    MetaAttribute deserialize(byte[] bytes);

    String getName();

    DataType getType();

    boolean getIsPrimaryKey();
}
