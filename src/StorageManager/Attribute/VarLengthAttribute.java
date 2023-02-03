package StorageManager.Attribute;

import Constants.Constant;

public class VarLengthAttribute extends Attribute{
    // length of type (CHAR, VARCHAR);
    private final int length;

    public VarLengthAttribute(String name, Constant.DataType type, int length) {
        super(name, type);
        this.length = length;
    }

    public int getLength() {
        return length;
    }
}
