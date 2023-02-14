import java.util.ArrayList;

public class Parser {

    private static boolean hasError;
    
    public static String doParse(ArrayList<Token> tokens) {

        if (tokens == null) {
            hasError = true;
            return "";
        }
        return "";
    }
}
