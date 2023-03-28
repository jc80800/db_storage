package main.SqlParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.Constants.Constant;
import main.Constants.Constant.PrepareResult;
import main.Constants.Helper;
import main.StorageManager.Data.Attribute;
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
        // accounts for multi-line commands
        if (input.length() == 0) {
            return PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }
        if (input.charAt(0) == '<') {
            if (input.charAt(input.length() - 1) != '>' || !input.substring(input.indexOf("<") + 1,
                input.indexOf(">")).equalsIgnoreCase(Constant.QUIT_CODE)) {
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
            case Constant.DELETE -> result = deleteCommand(tokens);
            case Constant.CREATE -> result = createCommand(tokens);
            case Constant.DISPLAY -> result = displayCommand(tokens);
            case Constant.INSERT -> result = insertCommand(tokens);
            case Constant.SELECT -> result = selectCommand(tokens);
            case Constant.DROP -> result = dropCommand(tokens);
            case Constant.ALTER -> result = alterCommand(tokens);
            default -> {
                System.out.println("Invalid command");
                result = PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }
        }
        return result;
    }

    private Constant.PrepareResult deleteCommand(String[] tokens) {
        try {

            String from = tokens[1].toUpperCase();
            String tableName = tokens[2];
            String where = tokens[3].toUpperCase();

            if(!from.equals(Constant.FROM) || !where.equals(Constant.WHERE)){
                return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }

            return storageManager.executeDelete(tableName, Arrays.copyOfRange(tokens, 4, tokens.length));
        } catch(ArrayIndexOutOfBoundsException e){
            return PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }
    }
    /**
     * Function to check if Alter command is properly formatted
     *
     * @param tokens user command
     * @return PrepareResult
     */
    private Constant.PrepareResult alterCommand(String[] tokens) {
        try {

            String token = tokens[1].toUpperCase();
            String tableName = tokens[2];
            String action = tokens[3].toUpperCase();

            if (!token.equals(Constant.TABLE) || (!action.equals(Constant.DROP) && !action.equals(Constant.ADD))) {
                return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }

            return storageManager.executeAlter(tableName, action, Arrays.copyOfRange(tokens,4, tokens.length));
        } catch(ArrayIndexOutOfBoundsException e) {
            return PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }
    }

    /**
     * Function to check if Select command is properly formatted
     *
     * @param tokens user command
     * @return
     */
    private Constant.PrepareResult selectCommand(String[] tokens) {
        try {

            String[] attributes;
            String table;
            String[] whereAttributes = null;
            String orderBy = null;

            // Combine the words together
            String temp = String.join(" ", tokens);
            if(!temp.contains("select") || !temp.contains("from")){
                return PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }

            // Split by "FROM" to get the first half
            String[] selectField = temp.split("from");

            // Check for keyword "select"
            String selectAttributes = selectField[0];
            String[] tempField = selectAttributes.split(" ");
            if(!tempField[0].trim().equals("select")){
                return PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }

            // Concat the fields back without "select" and split by "," to get the table attributes
            String tableAttributes = Helper.concatString(tempField, 1, tempField.length).trim();
            attributes = tableAttributes.split(",");
            attributes = Arrays.stream(attributes).map(String::trim).toArray(String[]::new);

            // Grab table in "from"
            String fromClause = selectField[1].trim();
            String[] fromClauseField = fromClause.split(" ");
            if(fromClauseField.length == 1) {
                table = fromClause;
                return storageManager.executeSelect(attributes, table, whereAttributes, orderBy);
            }

            table = fromClauseField[0];

            // Else check for where and orderby clauses
            if(fromClause.contains("orderby")){
                String[] orderbyClause = fromClause.split("orderby");
                orderBy = orderbyClause[1].trim();
            }

            if(fromClause.contains("where")){
                String[] whereClauseField = fromClause.split("where");
                String whereAttributesString = whereClauseField[1].trim();

                if(whereAttributesString.contains("orderby")){
                    String[] lastFieldTokens = whereAttributesString.split("orderby");
                    whereAttributesString = lastFieldTokens[0];
                    whereAttributes = Helper.implementShuntingYard(whereAttributesString);
                }
            }

            return storageManager.executeSelect(attributes, table, whereAttributes, orderBy);

        } catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {
            return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }
    }

    /**
     * Function to check if Drop command is properly formatted
     * @param tokens User command
     * @return
     */
    private Constant.PrepareResult dropCommand(String[] tokens) {
        try {
            String token = tokens[1].toUpperCase();
            if (tokens.length != 3 || !token.equals(Constant.TABLE)) {
                return PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }
            return storageManager.executeDrop(tokens[2]);

        } catch (ArrayIndexOutOfBoundsException e) {
            return PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }
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


            tokens = Arrays.copyOfRange(tokens, 4, tokens.length);
            String combined_values = String.join(" ", tokens);
            combined_values = combined_values.trim();

            String[] splitValues = combined_values.split(",");

            for (int i = 0; i < splitValues.length; i++){
                splitValues[i] = splitValues[i].replace("(", "");
                splitValues[i] = splitValues[i].replace(")", "");
                splitValues[i] = splitValues[i].trim();
            }

            return storageManager.executeInsert(table_name, splitValues);

        } catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {
            return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }
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
                return storageManager.displayInfo(tokens[2]);

            } else if (tokens.length == 2 && tokens[1].toUpperCase().equals(Constant.SCHEMA)) {
                return storageManager.displaySchema();
            } else {
                System.out.println("Invalid command");
                return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }
        } catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {
            return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }
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
            if(table_name.toString().equals("temp")){
                System.out.println("Do not name table as \" temp \"");
                return PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
            }

            tokens[2] = tokens[2].substring(index);

            tokens = Arrays.copyOfRange(tokens, 2, tokens.length);
            String combined_values = String.join(" ", tokens);
            combined_values = combined_values.replaceFirst("\\(", "");
            String[] values = combined_values.split(",");

            values[values.length - 1] = values[values.length - 1].substring(0,
                values[values.length - 1].length() - 1);

            return storageManager.createTable(table_name.toString(), values);
        } catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {
            return Constant.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;
        }

    }

}
