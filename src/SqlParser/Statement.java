package SqlParser;

import Constants.Constant;
import StorageManager.Metadata.Attribute.MetaAttribute;
import StorageManager.Metadata.Attribute.VarLengthMetaAttribute;
import StorageManager.StorageManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class Statement {

    private final StorageManager storageManager;

    public Statement(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    /**
     * Function to parse initial command and call respective functions
     * @param input
     * @return
     */
    public Constant.PrepareResult prepareStatement(String input) {
        String[] tokens = input.split(" ");
        String type = tokens[0].toUpperCase(Locale.ROOT);
        Constant.PrepareResult result;
        switch (type) {
            case Constant.CREATE ->
                result = createCommand(tokens, input);
            case Constant.DISPLAY ->
                result = displayCommand(tokens);
            case Constant.INSERT -> {
                result = insertCommand(tokens);
            }
            case Constant.SELECT -> {
                result = selectCommand(tokens);
            }
            case Constant.QUIT_CODE -> {
                result = Constant.PrepareResult.PREPARE_QUIT;
            }
            default -> result = Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }

        return result;
    }

    /**
     * Function to check if Select command is properly formatted
     * @param tokens user command
     * @return
     */
    private Constant.PrepareResult selectCommand(String[] tokens) {
        try{
            if (tokens.length != 4 || !tokens[1].equals("*") || !tokens[2].equals("from")){
                return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }
            storageManager.executeSelect(tokens[3]);

        } catch (ArrayIndexOutOfBoundsException e) {
            return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }

        return Constant.PrepareResult.PREPARE_SUCCESS;
    }

    /**
     * Parses the insert command and calls storage manager if valid
     * @param tokens
     * @return
     */
    private Constant.PrepareResult insertCommand(String[] tokens) {
        try{
            if(tokens.length < 5 || !tokens[1].equals("into") || !tokens[3].equals("values")){
                return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }

            String table_name = tokens[2];

            // Recombining and splitting the values based on comma

            tokens = Arrays.copyOfRange(tokens, 4, tokens.length);
            String combined_values = String.join(" ", tokens);
            String[] values = combined_values.split(",");

            storageManager.executeInsert(table_name, values);

        } catch (ArrayIndexOutOfBoundsException e) {
            return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }

        return Constant.PrepareResult.PREPARE_SUCCESS;
    }

    /**
     * Parses Display command calls storage manager if valid
     * @param tokens
     * @return
     */
    private Constant.PrepareResult displayCommand(String[] tokens) {
        try {
            if (tokens.length == 3 && tokens[1].equals(Constant.INFO)) {
                storageManager.displayInfo(tokens[2]);

            } else if(tokens.length == 2 && tokens[1].equals(Constant.SCHEMA)){
                storageManager.displaySchema();
            } else {
                return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }

        return Constant.PrepareResult.PREPARE_SUCCESS;
    }

    private Constant.PrepareResult createCommand(String[] tokens, String input) {
        // check if second word is "table"
        try {
            String token = tokens[1].toUpperCase();
            if (!token.equals(Constant.TABLE)) {
                return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }

            // start parsing after the "create table " for table name
            int nameIndex = tokens[1].length() + tokens[2].length() + 2;
            StringBuilder name = new StringBuilder();
            ArrayList<MetaAttribute> attributes = new ArrayList<>();
            while(input.charAt(nameIndex) == '(') {
                name.append(input.charAt(nameIndex));
                nameIndex += 1;
            }

            String[] attributeList = input.replace(");", "").split("\\(")[1].split(",");
            for (String s : attributeList) {
                System.out.println(s);
                String[] attribute = s.split(" ");
                String attributeType = attribute[1].toUpperCase();
                if(attributeType.equals("INTEGER")) {
                    attributes.add(new MetaAttribute(attribute[0], Constant.DataType.INTEGER));
                }
                else if(attributeType.equals("DOUBLE")) {
                    attributes.add(new MetaAttribute(attribute[0], Constant.DataType.DOUBLE));
                }
                else if(attributeType.equals("BOOLEAN")) {
                    attributes.add(new MetaAttribute(attribute[0], Constant.DataType.BOOLEAN));
                }
                else if(attributeType.contains("CHAR")) {
                    int length = Integer.parseInt(attribute[1].split("\\(")[1].replace(")", ""));
                    attributes.add(new VarLengthMetaAttribute(attribute[0], Constant.DataType.CHAR, length));
                    System.out.println(length);

                }
                else if(attributeType.contains("VARCHAR")) {
                    int length = Integer.parseInt(attribute[1].split("\\(")[1].replace(")", ""));
                    attributes.add(new VarLengthMetaAttribute(attribute[0], Constant.DataType.VARCHAR, length));
                    System.out.println(length);
                }
                else {
                    return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
                }
            }

            storageManager.createTable(name.toString(), attributes);

        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("GOT HER");
            return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }
        return Constant.PrepareResult.PREPARE_SUCCESS;
    }

}
