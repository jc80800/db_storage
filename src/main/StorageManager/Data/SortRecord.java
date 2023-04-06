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

        ArrayList<Attribute> tempAtt = record.getAttributes();
        boolean found = false;
        Attribute attribute = null;
        Attribute otherAttribute = null;
        for(int i = 0; i < tempAtt.size(); i++){
            Attribute attribute1 = tempAtt.get(i);
            if(attribute1.getMetaAttribute().getName().equals(attributeName)){
                // check if that attribute is duplicate
                if(record.checkDuplicateAttribute(attributeName)){
                    System.out.printf("Need to specify Duplicate attribute %s\n", attributeName);
                    throw new IllegalArgumentException();
                } else{
                    attribute = attribute1;
                    otherAttribute = ((SortRecord) o).record.getAttributes().get(i);
                    found = true;
                    break;
                }
            }
        }
        if (!found){
            attribute = record.getAttributeByName(attributeName);
            otherAttribute = ((SortRecord) o).record.getAttributeByName(this.attributeName);
        }
        if (attribute == null) {
            System.out.printf("No such attribute %s\n", attributeName);
            throw new IllegalArgumentException();
        }

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