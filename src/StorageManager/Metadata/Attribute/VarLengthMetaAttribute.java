package StorageManager.Metadata.Attribute;

import Constants.Constant;

public class VarLengthMetaAttribute extends MetaAttribute {

    // length of type (CHAR, VARCHAR);
    private final int length;

    public VarLengthMetaAttribute(String name, Constant.DataType type, int length) {
        super(name, type);
        this.length = length;
    }

    public int getLength() {
        return length;
    }
}
