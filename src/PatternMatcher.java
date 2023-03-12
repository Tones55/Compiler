import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class PatternMatcher {

    private static Pattern explicitIdentifier = Pattern.compile("a|c|d|e|g|h|j|k|l|m|n|o|q|r|u|v|x|y|z"); // used to match identifiers that can not be keywords
    private static Pattern keyword = Pattern.compile("print|while|if|int|string|boolean|true|false");
    private static Pattern identifier = Pattern.compile("[a-z]");
    private static Pattern symbol = Pattern.compile("\\{|\\}|\\(|\\)|==|!=|\\+");
    private static Pattern assign = Pattern.compile("=");
    private static Pattern digit = Pattern.compile("[0-9]");
    private static Pattern character = Pattern.compile("[a-z]|\\s"); // can only be found in quotes
    private static Pattern quote = Pattern.compile("\"");

    // used to aid in the creation of tokens but are not tokens themselves
    private static Pattern boundry = Pattern.compile("\\s|\\t|\\r"); // used to match whitespace
    private static Pattern endOfProgram = Pattern.compile("\\$");
    private static Pattern illegalCharacters = Pattern.compile("[^a-z0-9\\s\\t\\r\\{\\}\\(\\)\\=\\!=\\+\"\\$]");

    public static String match(String input , int t){
        Matcher m;
        m = explicitIdentifier.matcher(input);
        String matchName = "No Match";
        // the for loop and continue is used so all the ifs do not need to run
        // it never loops so continue sends it to the return statement
        for(int i=0;i<1;i++) {
            if(m.matches()){
                Lexer.updateToken("Identifier" , input , t);
                matchName = "Explicit Identifier";
                continue;
            }
            m = keyword.matcher(input);
            if(m.matches()){
                Lexer.updateToken("Keyword" , input , t);
                matchName = "Keyword";
                continue;
            }
            m = identifier.matcher(input);
            if(m.matches()){
                Lexer.updateToken("Identifier" , input , t);
                matchName = "Identifier";
                continue;
            }
            m = symbol.matcher(input);
            if(m.matches()){
                Lexer.updateToken("Symbol" , input , t);
                matchName = "Symbol";
                continue;
            }
            m = assign.matcher(input);
            if(m.matches()){
                Lexer.updateToken("Symbol" , input , t);
                matchName = "Assign";
                continue;
            }
            m = digit.matcher(input);
            if(m.matches()){
                Lexer.updateToken("Digit" , input , t);
                matchName = "Digit";
                continue;
            }
            m = quote.matcher(input);
            if(m.matches()){
                Lexer.updateToken("Quote", input, t);
                matchName = "Quote";
                continue;
            }
            m = endOfProgram.matcher(input);
            if(m.find()){
                Lexer.updateToken("EOF", input, t);
                matchName = "End of Program";
                continue;
            }
            // not tokens but used to aid in the creation of tokens
            m = boundry.matcher(input);
            if(m.matches()){
                matchName = "Boundry";
                continue;
            }
            m = illegalCharacters.matcher(input);
            if(m.matches()){
                matchName = "Illegal Character";
                continue;
            }
        }
        return matchName;
    }

    public static String checkForCharList(String input) {
        Matcher m = keyword.matcher(input);
        m = character.matcher(input);
        String matchName = "No Match";
        // the for loop and continue is used so all the ifs do not need to run
        // it never loops so continue sends it to the return statement
        for (int i = 0; i < 1; i++) {
            if(m.matches()){
                Lexer.updateToken("Character" , input , 1);
                matchName = "Character";
                continue;
            }
            m = quote.matcher(input);
            if(m.matches()){
                Lexer.updateToken("Quote", input, 1);
                matchName = "Quote";
                continue;
            }
        }
        return matchName;
    }
}