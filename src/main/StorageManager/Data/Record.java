package main.StorageManager.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import main.Constants.Constant;
import main.Constants.Constant.DataType;
import main.Constants.Helper;
import main.StorageManager.MetaData.MetaAttribute;
import main.StorageManager.MetaData.MetaTable;

public class Record {

    private ArrayList<Attribute> attributes;
    private ArrayList<MetaAttribute> metaAttributes;
    private int binarySize;

    public Record(ArrayList<Attribute> attributes,
        ArrayList<MetaAttribute> metaAttributes) {
        this.attributes = attributes;
        this.metaAttributes = metaAttributes;
        this.binarySize = calculateBinarySize();
    }

    public Record(ArrayList<Attribute> attributes, ArrayList<MetaAttribute> metaAttributes,
        int binarySize) {
        this.attributes = attributes;
        this.metaAttributes = metaAttributes;
        this.binarySize = binarySize;
    }

    public static Record deserialize(byte[] bytes, ArrayList<MetaAttribute> metaAttributes) {
        byte[] fieldBoolean = Arrays.copyOf(bytes, metaAttributes.size());
        ArrayList<Attribute> attributes = new ArrayList<>();

        int index = metaAttributes.size();
        for (int i = 0; i < fieldBoolean.length; i++) {
            byte ifNull = fieldBoolean[i];
            MetaAttribute metaAttribute = metaAttributes.get(i);
            DataType type = metaAttribute.getType();

            if (ifNull == 0) {
                attributes.add(new Attribute(metaAttribute, null));
            }

            switch (type) {
                case BOOLEAN -> {
                    byte[] valueBytes = new byte[]{bytes[index]};
                    Attribute attribute = Attribute.deserialize(valueBytes, metaAttribute);
                    index += attribute.getBinarySize();
                    attributes.add(attribute);
                }

                case INTEGER -> {
                    byte[] valueBytes = Arrays.copyOfRange(bytes, index,
                        index + Constant.INTEGER_SIZE);
                    Attribute attribute = Attribute.deserialize(valueBytes, metaAttribute);
                    index += attribute.getBinarySize();
                    attributes.add(attribute);
                }

                case DOUBLE -> {
                    byte[] valueBytes = Arrays.copyOfRange(bytes, index,
                        index + Constant.DOUBLE_SIZE);
                    Attribute attribute = Attribute.deserialize(valueBytes, metaAttribute);
                    index += attribute.getBinarySize();
                    attributes.add(attribute);
                }

                case CHAR -> {
                    byte[] valueBytes = Arrays.copyOfRange(bytes, index,
                        index + metaAttribute.getMaxLength());
                    Attribute attribute = Attribute.deserialize(valueBytes, metaAttribute);
                    index += attribute.getBinarySize();
                    attributes.add(attribute);
                }

                case VARCHAR -> {
                    int valueLen = Helper.convertByteArrayToInt(Arrays.copyOfRange(bytes, index,
                        index + Constant.INTEGER_SIZE));
                    index += Constant.INTEGER_SIZE;
                    Attribute attribute = Attribute.deserialize(
                        Arrays.copyOfRange(bytes, index, index + valueLen), metaAttribute);
                    index += attribute.getBinarySize();
                    attributes.add(attribute);
                }
            }
        }
        return new Record(attributes, metaAttributes, bytes.length);
    }

    public static ArrayList<Record> parseRecords(String[] values, MetaTable metaTable) {
        ArrayList<MetaAttribute> metaAttributes = metaTable.metaAttributes();

        ArrayList<Record> result = new ArrayList<>();

        for (String value : values) {
            ArrayList<Attribute> recordAttribute = new ArrayList<>();
            value = value.replace("(", "");
            value = value.replace(")", "");
            System.out.println(value);

            String[] tokens = value.split(" ");
            boolean completion = true;

            for (int i = 0; i < metaAttributes.size(); i++) {
                String object = tokens[i];
                MetaAttribute metaAttribute = metaAttributes.get(i);
                DataType dataType = metaAttribute.getType();

                if (object.charAt(0) == '\"' && object.charAt(tokens.length - 1) == '\"') {
                    if (dataType.equals(DataType.VARCHAR)) {
                        // check size
                        if (object.length() - 2 <= metaAttribute.getMaxLength()) {
                            recordAttribute.add(
                                new Attribute(metaAttribute, object.substring(1,
                                    object.length() - 1)));
                        } else {
                            System.out.println("String has more character than MetaAttribute");
                            completion = false;
                            break;
                        }
                    }
                } else if (Helper.checkInteger(object) && dataType
                    .equals(DataType.INTEGER)) {
                    int intObject = Integer.parseInt(object);
                    recordAttribute.add(new Attribute(metaAttribute, intObject));
                } else if (Helper.checkDouble(object) && dataType.equals(DataType.DOUBLE)) {
                    double doubleObject = Double.parseDouble(object);
                    recordAttribute.add(new Attribute(metaAttribute, doubleObject));
                } else {
                    if (!Helper.checkBoolean(tokens[i]) || !dataType.equals(DataType.BOOLEAN)) {
                        System.out.println("Incorrect data type");
                        completion = false;
                        break;
                    }
                    boolean boolObject = true;
                    if(object.equals("false")){
                        boolObject = false;
                    }
                    recordAttribute.add(new Attribute(metaAttribute, boolObject));
                }
            }
            if(completion){
                result.add(new Record(recordAttribute, metaAttributes));
            } else {
                break;
            }
        }
        return result;
    }

    public int getBinarySize() {
        return binarySize;
    }

    private int calculateBinarySize() {
        int binarySize = attributes.size();
        for (Attribute attribute : attributes) {
            // VARCHAR has a leading int indicating the length of the string
            if (attribute.getMetaAttribute().getType() == DataType.VARCHAR) {
                binarySize += Constant.INTEGER_SIZE + attribute.getBinarySize();
            } else {
                binarySize += attribute.getBinarySize();
            }
        }
        return binarySize;
    }

    /**
     * form: [fieldMap, attributes]
     *
     * @return
     */
    public byte[] serialize() {

        byte[] fieldBoolean = new byte[this.attributes.size()];
        for (int i = 0; i < this.attributes.size(); i++) {
            if (this.attributes.get(i).getValue() == null) {
                fieldBoolean[i] = 0;
            } else {
                fieldBoolean[i] = 1;
            }
        }

        for (Attribute attribute : this.attributes) {
            if (attribute.getMetaAttribute().getType() == DataType.VARCHAR) {
                byte[] valueBytes = attribute.serialize();
                byte[] valueLen = Helper.convertIntToByteArray(valueBytes.length);
                fieldBoolean = Helper.concatenate(fieldBoolean, valueLen, valueBytes);
            } else {
                fieldBoolean = Helper.concatenate(fieldBoolean, attribute.serialize());
            }
        }
        return fieldBoolean;
    }

    public Attribute getPrimaryKey(){
        for(Attribute attribute : this.attributes){
            if(attribute.checkPrimaryKey()){
                return attribute;
            }
        }
        return null;
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(ArrayList<Attribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Record record = (Record) o;
        return attributes.equals(record.attributes) && metaAttributes.equals(record.metaAttributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributes, metaAttributes);
    }

    @Override
    public String toString() {
        return "Record{" +
            "attributes=" + attributes +
            ", metaAttributes=" + metaAttributes +
            '}';
    }
}
