package Constants;

/**
 * Class to store src.Constants.Constants needed for the program
 */
public final class Constant {

    public static final String QUIT_CODE = "QUIT";
    public static final String PROMPT = "> ";
    public static final String INSERT = "INSERT";
    public static final String SELECT = "SELECT";
    public static final String DISPLAY = "DISPLAY";
    public static final String CREATE = "CREATE";
    public static final String TABLE = "TABLE";
    public static final String SCHEMA = "SCHEMA";
    public static final String INFO = "INFO";
    public static final String INTEGER = "INTEGER";
    public static final String DOUBLE = "DOUBLE";
    public static final String BOOLEAN = "BOOLEAN";
    public static final String CHAR = "CHAR";
    public static final String VARCHAR = "VARCHAR";

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
}