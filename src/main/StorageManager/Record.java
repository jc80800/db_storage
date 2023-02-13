package main.StorageManager;

import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.crypto.Data;
import main.Constants.Constant;
import main.Constants.Constant.DataType;
import main.Constants.Helper;

public class Record {

    private byte[] byteArray;
    private ArrayList<Attribute> attributes;

    public Record(ArrayList<Attribute> attributes, byte[] byteArray) {
        this.attributes = attributes;
        this.byteArray = byteArray;
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(ArrayList<Attribute> attributes) {
        this.attributes = attributes;
    }

    public byte[] serialize(){

        byte[] fieldBoolean = new byte[this.attributes.size()];
        for (int i = 0; i < this.attributes.size(); i++){
            if (this.attributes.get(i).getValue() == null){
                fieldBoolean[i] = 0;
            } else {
                fieldBoolean[i] = 1;
            }
        }

        for (int i = 0; i < this.attributes.size(); i ++){
            fieldBoolean = Helper.concatenate(fieldBoolean, this.attributes.get(i).getValue());
        }

        return fieldBoolean;
    }

    public static Record deserialize(byte[] bytes, ArrayList<DataType> dataTypes, int numOfAttributes) {
        byte[] fieldBoolean = Arrays.copyOf(bytes, numOfAttributes);
        byte[] values = Arrays.copyOfRange(bytes, numOfAttributes + 1, bytes.length);

        int pointer = 0;
        for(int i = 0; i < fieldBoolean.length; i++){
            byte ifNull = fieldBoolean[i];
            if (ifNull == 0){
                continue;
            }

            switch (dataTypes.get(i)) {
                case INTEGER:
                    int endPoint = pointer + Constant.INTEGER_SIZE;
                    // Read from pointer to endPoint

                    // read the next 4 bytes in values

                    break;
                case BOOLEAN:
                    // read the next 1 byte in values
                    break;
                case DOUBLE:
                    // read the next 2 bytes in values
                    break;
                case CHAR:
                    // read in the x amount
                    break;
                case VARCHAR:
                    break;
            }
        }
        return new Record(null, null);
    }
}
