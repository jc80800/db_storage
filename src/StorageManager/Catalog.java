package StorageManager;

import StorageManager.Attribute.Attribute;

import java.util.ArrayList;

public class Catalog {
    private final String name;
    private ArrayList<Attribute> attributes;

    public Catalog(String name, ArrayList<Attribute> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(ArrayList<Attribute> attributes) {
        this.attributes = attributes;
    }
}
