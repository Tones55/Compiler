package src;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class PatternMatcher {

    private static Pattern explicitIdentifier = Pattern.compile("acdeghjklmnopqruvxyz"); //used to match identifiers that are not keywords
    private static Pattern keyword = Pattern.compile("print|while|if|int|string|boolean|true|false");
    private static Pattern identifier = Pattern.compile("[a-z]");
    private static Pattern symbol = Pattern.compile("\\{|\\}|\\(|\\)|==|!=|\\+");
    private static Pattern digit = Pattern.compile("[0-9]");
    private static Pattern character = Pattern.compile("[a-z]|\\s");
    private static Pattern quote = Pattern.compile("\"");

    //used to aid in the creation of tokens but are not tokens themselves
    private static Pattern boundry = Pattern.compile("☺"); //used to match whitespace
    private static Pattern endOfProgram = Pattern.compile("$");

    public static String match(String input , Position pos , int t){
        Matcher m;
        m = explicitIdentifier.matcher(input);
        if(m.find()){
            Lexer.updateToken("Identifier" , input , pos , t);
            return "Explicit Identifier";
        }
        m = keyword.matcher(input);
        if(m.find()){
            Lexer.updateToken("Keyword" , input , pos , t);
            return "Keyword";
        }
        m = identifier.matcher(input);
        if(m.find()){
            Lexer.updateToken("Identifier" , input , pos , t);
            return "Identifier";
        }
        m = symbol.matcher(input);
        if(m.find()){
            Lexer.updateToken("Symbol" , input , pos , t);
            return "Symbol";
        }
        m = digit.matcher(input);
        if(m.find()){
            Lexer.updateToken("Digit" , input , pos , t);
            return "Digit";
        }
        m = character.matcher(input);
        if(m.find()){
            Lexer.updateToken("Character" , input , pos , t);
            return "Character";
        }
        m = quote.matcher(input);
        if(m.find()){
            Lexer.updateToken("Quote", input, pos , t);
            return "Quote";
        }

        m = boundry.matcher(input);
        if(m.find()){
            //System.out.println("Boundry: " + input);
            return "Boundry";
        }
        m = endOfProgram.matcher(input);
        if(m.find()){
            return "End of Program";
        }
        return "No Match";
    }

    public static String checkForCharList(String input , Position pos) {
        Matcher m = keyword.matcher(input);
        m = character.matcher(input);
        if(m.find()){
            Lexer.updateToken("Character" , input , pos , 1);
            return "Character";
        }
        m = quote.matcher(input);
        if(m.find()){
            Lexer.updateToken("Quote", input, pos , 1);
            return "Quote";
        }
        return "No Match";
    }
}