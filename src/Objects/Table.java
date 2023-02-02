package Objects;

import java.util.ArrayList;

public class Table {
    private final String filePath;
    int numOfPages;
    ArrayList<Attribute> attributes;

    public Table(String filePath, ArrayList<Attribute> attributes) {
        this.filePath = filePath;
        this.numOfPages = 0;
        this.attributes = attributes;
    }
}
