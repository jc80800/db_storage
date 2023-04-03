package main.StorageManager.Data;

import main.StorageManager.MetaData.MetaAttribute;

import java.util.ArrayList;


public class SortRecord implements Comparable {
    private Record record;
    private String attributeName;

    public SortRecord(Record record, String attributeName) {
        this.record = record;
        this.attributeName = attributeName;
    }

    public Record getRecord() {
        return record;
    }

    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public int compareTo(Object o) {
        Attribute attribute = this.record.getAttribute(this.attributeName);
        Attribute otherAttribute = ((SortRecord) o).record.getAttribute(this.attributeName);

        Object value = attribute.getValue();
        Object otherValue = otherAttribute.getValue();

        MetaAttribute metaAttribute = attribute.getMetaAttribute();

        switch (metaAttribute.getType()) {
            case BOOLEAN -> {
                Boolean newValue = (boolean) value;
                Boolean newOtherValue = (boolean) otherValue;
                return newValue.compareTo(newOtherValue);
            }
            case INTEGER -> {
                Integer newValue = (int) value;
                Integer newOtherValue = (int) otherValue;
                return newValue.compareTo(newOtherValue);
            }
            case DOUBLE -> {
                Double newValue = (double) value;
                Double newOtherValue = (double) otherValue;
                return newValue.compareTo(newOtherValue);
            }

            case CHAR -> {
                Character newValue = (char) value;
                Character newOtherValue = (char) otherValue;
                return newValue.compareTo(newOtherValue);
            }
            case VARCHAR -> {
                String newValue = (String) value;
                String newOtherValue = (String) otherValue;
                return newValue.compareTo(newOtherValue);
            }
        }
        return 0;
    }


}
