package SqlParser;

import Constants.Constant;

import java.util.Locale;

public class Statement {

    Constant.StatementType type;

    public Constant.PrepareResult prepareStatement(String input) {
        String[] tokens = input.split(" ");
        String type = tokens[0].toUpperCase(Locale.ROOT);
        Constant.PrepareResult result;
        switch (type) {
            case Constant.CREATE -> {
                result = createCommand(tokens);
            }
            case Constant.DISPLAY -> {
                result = displayCommand(tokens);
            }
            case Constant.INSERT -> {
                this.type = Constant.StatementType.INSERT;
                result = Constant.PrepareResult.PREPARE_SUCCESS;
            }
            case Constant.SELECT -> {
                this.type = Constant.StatementType.SELECT;
                result = Constant.PrepareResult.PREPARE_SUCCESS;
            }
            case Constant.QUIT_CODE -> {
                this.type = Constant.StatementType.QUIT;
                result = Constant.PrepareResult.PREPARE_QUIT;
            }
            default -> result = Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }

        return result;
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

    public Constant.PrepareResult createCommand(String[] commandTokens) {
        // check if second word is "table"
        try {
            String token = commandTokens[1].toUpperCase();
            if (!token.equals(Constant.TABLE)) {
                return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }
        } catch (ArrayIndexOutOfBoundsException e){
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
            case INSERT -> {
                System.out.println("Insert statement executing");
            }
            case SELECT -> {
                System.out.println("Select statement executing");
            }
            case CREATE_TABLE -> {
                System.out.println("Create Table statement executing");
            }
            case DISPLAY_INFO -> {
                System.out.println("Display info statement executing");
            }
            case DISPLAY_SCHEMA -> {
                System.out.println("Display schema statement executing");
            }
        }
    }
}
