package main.StorageManager;

import main.Constants.Constant.DataType;

class Attribute {

    private final DataType name;
    private byte[] value;

    public Attribute(DataType name, byte[] value) {
        this.name = name;
        this.value = value;
    }

    public DataType getName() {
        return name;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

}
