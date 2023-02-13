package main.StorageManager;

import java.util.ArrayList;

public class Record {

    ArrayList<Attribute> attributes;

    public Record(ArrayList<Attribute> attributes) {
        this.attributes = attributes;
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(ArrayList<Attribute> attributes) {
        this.attributes = attributes;
    }

    private class Attribute {

        private final String name;
        private ArrayList<Byte> value;

        public Attribute(String name, ArrayList<Byte> value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public ArrayList<Byte> getValue() {
            return value;
        }

        public void setValue(ArrayList<Byte> value) {
            this.value = value;
        }
    }
}
