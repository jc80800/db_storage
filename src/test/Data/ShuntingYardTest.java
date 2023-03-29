package test.Data;

import java.util.Queue;
import main.Constants.Helper;
import main.SqlParser.ShuntingYardAlgorithm;
import org.junit.jupiter.api.Test;

public class ShuntingYardTest {
    @Test
    void testAlgo(){
        Queue<String> output = ShuntingYardAlgorithm.parse("age > 10 AND name = \"bar\"");
        System.out.println(output);
    }

}
