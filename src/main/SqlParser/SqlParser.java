package main.SqlParser;

import java.util.Arrays;
import java.util.Locale;
import main.Constants.Constant;
import main.Constants.Constant.PrepareResult;
import main.StorageManager.StorageManager;

public class SqlParser {

    private final StorageManager storageManager;

    public SqlParser(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    /**
     * Function to parse initial command and call respective functions
     *
     * @param input
     * @return
     */
    public PrepareResult prepareStatement(String input) {
        if (input.charAt(0) == '<') {
            if (input.charAt(input.length() - 1) != '>' || !input.substring(input.indexOf("<") + 1,
                input.indexOf(">")).equalsIgnoreCase(Constant.QUIT_CODE)) {
                System.out.println("ERROR");
                return PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }
            return PrepareResult.PREPARE_QUIT;
        }
        char lastChar = input.charAt(input.length() - 1);
        if (lastChar != ';') {
            System.out.println("Invalid format, missing semicolon at the end \";\"");
            return PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }
        input = input.substring(0, input.length() - 1);
        String[] tokens = input.split("\\s+");
        String type = tokens[0].toUpperCase(Locale.ROOT);
        PrepareResult result;
        switch (type) {
            case Constant.CREATE -> result = createCommand(tokens);
            case Constant.DISPLAY -> result = displayCommand(tokens);
            case Constant.INSERT -> result = insertCommand(tokens);
            case Constant.SELECT -> result = selectCommand(tokens);
            default -> {
                System.out.println("Invalid command");
                result = PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }
        }
        return result;
    }

    /**
     * Function to check if Select command is properly formatted
     *
     * @param tokens user command
     * @return
     */
    private Constant.PrepareResult selectCommand(String[] tokens) {
        try {
            if (tokens.length != 4 || !tokens[1].equals("*") || !tokens[2].equals("from")) {
                return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }
            storageManager.executeSelect(tokens[3]);

        } catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {
            return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }

        return Constant.PrepareResult.PREPARE_SUCCESS;
    }

    /**
     * Parses the insert command and calls storage manager if valid
     *
     * @param tokens
     * @return
     */
    private Constant.PrepareResult insertCommand(String[] tokens) {
        try {
            if (tokens.length < 5 || !tokens[1].equals("into") || !tokens[3].contains("values")) {
                return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }

            String table_name = tokens[2];

            int index = 0;
            while (index < tokens[3].length() && !(tokens[3].charAt(index) == '(')) {
                index += 1;
            }

            tokens[3] = tokens[3].substring(index);

            tokens = Arrays.copyOfRange(tokens, 3, tokens.length);
            String combined_values = String.join(" ", tokens);
            combined_values = combined_values.replace("(", "");
            String[] values = combined_values.split(",");

            values[values.length - 1] = values[values.length - 1].substring(0,
                values[values.length - 1].length() - 1);

            storageManager.executeInsert(table_name, values);

        } catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {
            return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }

        return Constant.PrepareResult.PREPARE_SUCCESS;
    }

    /**
     * Parses Display command calls storage manager if valid
     *
     * @param tokens
     * @return
     */
    private Constant.PrepareResult displayCommand(String[] tokens) {
        try {
            if (tokens.length == 3 && tokens[1].toUpperCase().equals(Constant.INFO)) {
                storageManager.displayInfo(tokens[2]);

            } else if (tokens.length == 2 && tokens[1].toUpperCase().equals(Constant.SCHEMA)) {
                storageManager.displaySchema();
            } else {
                System.out.println("Invalid command");
                return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }
        } catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {
            return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }

        return Constant.PrepareResult.PREPARE_SUCCESS;
    }

    private Constant.PrepareResult createCommand(String[] tokens) {
        // check if second word is "table"
        try {
            String token = tokens[1].toUpperCase();
            if (!token.equals(Constant.TABLE)) {
                return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }

            StringBuilder table_name = new StringBuilder();
            int index = 0;
            while (index < tokens[2].length() && !(tokens[2].charAt(index) == '(')) {
                table_name.append(tokens[2].charAt(index));
                index += 1;
            }

            tokens[2] = tokens[2].substring(index);

            tokens = Arrays.copyOfRange(tokens, 2, tokens.length);
            String combined_values = String.join(" ", tokens);
            combined_values = combined_values.replaceFirst("\\(", "");
            String[] values = combined_values.split(",");

            values[values.length - 1] = values[values.length - 1].substring(0,
                values[values.length - 1].length() - 1);

            storageManager.createTable(table_name.toString(), values);
        } catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {
            return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }
        return PrepareResult.PREPARE_SUCCESS;
    }

}