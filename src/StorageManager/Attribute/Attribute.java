package StorageManager.Attribute;

import Constants.Constant;

public class Attribute {
    private final String name;
    private final Constant.DataType type;

    public Attribute(String name, Constant.DataType type) {
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
