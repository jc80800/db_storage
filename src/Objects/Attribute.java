package Objects;

import Constants.Constant;

public class Attribute {
    private final String name;
    private final Constant.DataType type;
    // length of type (CHAR, VARCHAR);
    private int length;

    public Attribute(String name, Constant.DataType type) {
        this.name = name;
        this.type = type;
    }

    public Attribute(String name, Constant.DataType type, int length) {
        this.name = name;
        this.type = type;
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public String getName() {
        return name;
    }

    public Constant.DataType getType() {
        return type;
    }
}
