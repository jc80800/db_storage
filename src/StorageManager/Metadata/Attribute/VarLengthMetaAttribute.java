package StorageManager.Metadata.Attribute;

import Constants.Constant;
import Constants.Constant.DataType;
import java.util.Arrays;

public class VarLengthMetaAttribute extends MetaAttribute {

    // length of type (CHAR, VARCHAR);
    private final Integer length;

    public VarLengthMetaAttribute(String name, Constant.DataType type, Boolean isPrimaryKey, Integer length) {
        super(name, type, isPrimaryKey);
        this.length = length;
    }

    public Integer getLength() {
        return length;
    }
}
