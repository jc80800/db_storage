package StorageManager.Metadata.Attribute;

import Constants.Constant;
import Constants.Constant.DataType;
import java.util.Arrays;

public class VarLengthMetaAttribute extends MetaAttribute {

    // length of type (CHAR, VARCHAR);
    private final int length;

    public VarLengthMetaAttribute(String name, Constant.DataType type, int length) {
        super(name, type);
        this.length = length;
    }

    /*
    @Override
    public VarLengthMetaAttribute deserialize(byte[] input) {
        int index = 0;
        int nameLength = ((int) input[index]) & 0xFF;
        index++;
        byte[] nameArray = Arrays.copyOfRange(input, index, index + nameLength + 1);
        index += nameLength + 1;
        String name = new String(nameArray);

        int typeLength = ((int) input[index]) & 0xFF;
        index++;
        byte[] typeArray = Arrays.copyOfRange(input, index, index + typeLength + 1);
        String typeString = new String(typeArray);
        Constant.DataType type;
        if (typeString.startsWith(Constant.CHAR)) {
            type = DataType.CHAR;

        } else {
            type = DataType.VARCHAR;
        }
        int length = Integer.parseInt(
            typeString.substring(typeString.indexOf("(") + 1, typeString.indexOf(")")));
        return new VarLengthMetaAttribute(name, type, length);
    }

     */

    public int getLength() {
        return length;
    }
}
