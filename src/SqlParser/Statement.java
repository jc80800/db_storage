package SqlParser;

import Constants.Constant;
import StorageManager.Metadata.Attribute.MetaAttribute;
import StorageManager.Metadata.Attribute.VarLengthMetaAttribute;
import StorageManager.StorageManager;

import java.util.ArrayList;
import java.util.Locale;

public class Statement {

    private Constant.StatementType type;

    private final StorageManager storageManager;

    public Statement(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

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
                this.type = Constant.StatementType.INSERT;
                result = insertCommand(tokens);
            }
            case Constant.SELECT -> {
                this.type = Constant.StatementType.SELECT;
                result = selectCommand(tokens);
            }
            case Constant.QUIT_CODE -> {
                this.type = Constant.StatementType.QUIT;
                result = Constant.PrepareResult.PREPARE_QUIT;
            }
            default -> result = Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }

        return result;
    }
    private Constant.PrepareResult selectCommand(String[] tokens) {
        try{
            String token = tokens[1].toUpperCase();
            if (!(token.equals(Constant.SELECT))){
                return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }

        return Constant.PrepareResult.PREPARE_SUCCESS;
    }
    private Constant.PrepareResult insertCommand(String[] tokens) {
        try{
            String token = tokens[1].toUpperCase();
            if (!(token.equals(Constant.INSERT))){
                return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }

        return Constant.PrepareResult.PREPARE_SUCCESS;
    }
    private Constant.PrepareResult displayCommand(String[] tokens) {
        try {
            String token = tokens[1].toUpperCase();
            if (!(token.equals(Constant.INFO) || token.equals(Constant.SCHEMA))) {
                return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }
            if (token.equals(Constant.INFO)) {
                this.type = Constant.StatementType.DISPLAY_INFO;
            } else {
                this.type = Constant.StatementType.DISPLAY_SCHEMA;
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
        this.type = Constant.StatementType.CREATE_TABLE;
        return Constant.PrepareResult.PREPARE_SUCCESS;
    }

    public void execute() {
        switch (this.type) {
            case QUIT -> {
                System.out.println("Quit statement executing");
                System.exit(0);
            }
            case INSERT ->
                System.out.println("Insert statement executing");
            case SELECT ->
                System.out.println("Select statement executing");
            case CREATE_TABLE ->
                System.out.println("Create Table statement executing");
            case DISPLAY_INFO ->
                System.out.println("Display info statement executing");
            case DISPLAY_SCHEMA ->
                System.out.println("Display schema statement executing");
        }
    }
}
