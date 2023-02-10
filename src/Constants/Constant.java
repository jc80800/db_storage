package Constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

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
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final Integer INTEGER_SIZE = 4;
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
}