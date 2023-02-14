package main.StorageManager.Data;

import java.util.ArrayList;
import main.StorageManager.Data.Attribute;
import main.StorageManager.Metadata.MetaAttribute;

public class Record {

    private byte[] byteArray;
    private ArrayList<Attribute> attributes;
    private ArrayList<MetaAttribute> metaAttributes;

    public Record(byte[] byteArray, ArrayList<Attribute> attributes,
        ArrayList<MetaAttribute> metaAttributes) {
        this.attributes = attributes;
        this.byteArray = byteArray;
        this.metaAttributes = metaAttributes;
    }

//    public static Record deserialize(byte[] bytes, ArrayList<MetaAttribute> metaAttributes,
//        int numOfAttributes) {
//        byte[] fieldBoolean = Arrays.copyOf(bytes, numOfAttributes);
//        ArrayList<Attribute> attributes = new ArrayList<>();
//
//        int index = numOfAttributes;
//
//        for (int i = 0; i < fieldBoolean.length; i++) {
//            byte ifNull = fieldBoolean[i];
//            MetaAttribute metaAttribute = metaAttributes.get(i);
//            DataType type = metaAttribute.getType();
//
//            if (ifNull == 0) {
//                attributes.add(new Attribute(metaAttribute, null));
//            }
//
//            switch (type) {
//                case INTEGER -> {
//                    byte[] value = Arrays.copyOfRange(bytes, index, index + Constant.INTEGER_SIZE);
//                    index += Constant.INTEGER_SIZE;
//                    attributes.add(new Attribute(metaAttribute, value));
//                }
//
//                case BOOLEAN -> {
//                    byte[] value = Arrays.copyOfRange(bytes, index, index + Constant.BOOLEAN_SIZE);
//                    index += Constant.BOOLEAN_SIZE;
//                    attributes.add(new Attribute(metaAttribute, value));
//                }
//
//                case DOUBLE -> {
//                    byte[] value = Arrays.copyOfRange(bytes, index, index + Constant.DOUBLE_SIZE);
//                    index += Constant.DOUBLE_SIZE;
//                    attributes.add(new Attribute(metaAttribute, value));
//                }
//                // read the next 2 bytes in values
//
//                case CHAR -> {
//                    byte[] value = Arrays.copyOfRange(bytes, index,
//                        index + metaAttribute.getMaxLength());
//                    index += metaAttribute.getMaxLength();
//                    attributes.add()
//                }
//                // read in the x amount
//
//                case VARCHAR -> {
//                    int len = Helper.convert
//                    byte[] value = Arrays.copyOfRange(bytes, index, index + Constant.BOOLEAN_SIZE);
//                    index += Constant.BOOLEAN_SIZE;
//                    attributes.add(new Attribute(type, value));
//                }
//            }
//        }
//        return new Record(null, null);
//    }

    /**
     * form: [fieldMap, attributes]
     *
     * @return
     */
//    public byte[] serialize() {
//
//        byte[] fieldBoolean = new byte[this.attributes.size()];
//        for (int i = 0; i < this.attributes.size(); i++) {
//            if (this.attributes.get(i).getValue() == null) {
//                fieldBoolean[i] = 0;
//            } else {
//                fieldBoolean[i] = 1;
//            }
//        }
//
//        for (Attribute attribute : this.attributes) {
//            fieldBoolean = Helper.concatenate(fieldBoolean, attribute.getValue());
//        }
//
//        return fieldBoolean;
//    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(ArrayList<Attribute> attributes) {
        this.attributes = attributes;
    }

}
