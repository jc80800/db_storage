package StorageManager.Metadata.Attribute;

import Constants.Constant;
import Constants.Constant.DataType;
import java.util.Arrays;

public class MetaAttribute {

    private final String name;
    private final DataType type;

    // byte: length of name, string name, number of length of type, type

    protected MetaAttribute(String name, DataType type) {
        this.name = name;
        this.type = type;
    }

    public MetaAttribute deserialize(byte[] input) {
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
        DataType type = switch (typeString) {
            case Constant.INTEGER -> DataType.INTEGER;
            case Constant.BOOLEAN -> DataType.BOOLEAN;
            case Constant.DOUBLE -> DataType.DOUBLE;
        };
        return new MetaAttribute(name, type);
    }

    public String getName() {
        return name;
    }

    public Constant.DataType getType() {
        return type;
    }
}
