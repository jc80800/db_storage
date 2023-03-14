package main.Constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to store src.main.Constants.main.Constants needed for the program
 */
public final class Constant {

    public static final String QUIT_CODE = "QUIT";
    public static final String PROMPT = "> ";
    public static final String DELETE = "DELETE";
    public static final String INSERT = "INSERT";
    public static final String SELECT = "SELECT";
    public static final String DISPLAY = "DISPLAY";
    public static final String CREATE = "CREATE";
    public static final String ALTER = "ALTER";
    public static final String DROP = "DROP";
    public static final String ADD = "ADD";
    public static final String TABLE = "TABLE";
    public static final String DEFAULT = "DEFAULT";
    public static final String SCHEMA = "SCHEMA";
    public static final String INFO = "INFO";
    public static final String CATALOG_FILE = "/Catalog";
    public static final String TEMP = "TEMP";
    public static final String FROM = "FROM";
    public static final String WHERE = "WHERE";
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final Integer BOOLEAN_SIZE = 1;
    public static final Integer INTEGER_SIZE = Integer.BYTES;
    public static final Integer DOUBLE_SIZE = Double.BYTES;
    public static final Integer INITIAL_POINTER_SIZE = 10;
    public static final Integer NOT_NULL_CODE = 1;
    public static final Integer UNIQUE_CODE = 2;
    public static final HashMap<Integer, DataType> DATA_TYPE_MAP = new HashMap<>(){{
        put(1, DataType.INTEGER);
        put(2, DataType.DOUBLE);
        put(3, DataType.BOOLEAN);
        put(4, DataType.CHAR);
        put(5, DataType.VARCHAR);
    }};

    public enum StatementType {
        CREATE_TABLE,
        INSERT,
        SELECT,
        DISPLAY_SCHEMA,
        DISPLAY_INFO,
        QUIT
    }

    public enum PrepareResult {
        PREPARE_SUCCESS,
        PREPARE_UNRECOGNIZED_STATEMENT,
        PREPARE_QUIT
    }

    public enum DataType {
        INTEGER,
        DOUBLE,
        BOOLEAN,
        CHAR,
        VARCHAR
    }

    public static Set<String> getConstraints(){
        Set<String> possible_constraints = new HashSet<>();

        possible_constraints.add("notnull");
        possible_constraints.add("unique");
        return possible_constraints;
    }
}