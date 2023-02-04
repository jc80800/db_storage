package StorageManager.Metadata.Attribute;

import Constants.Constant;

public class MetaAttribute {

    private final String name;
    private final Constant.DataType type;

    public MetaAttribute(String name, Constant.DataType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Constant.DataType getType() {
        return type;
    }
}
