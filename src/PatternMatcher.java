package src;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class PatternMatcher {

    private static Pattern explicitIdentifier; //used to match identifiers that are not keywords
    private static Pattern keyword;
    private static Pattern identifier;
    private static Pattern symbol;
    private static Pattern digit;
    private static Pattern character;
    private static Pattern quote;

    //used to aid in the creation of tokens but are not tokens themselves
    private static Pattern boundry;
    
    public PatternMatcher(){
        /*
        keyword = Pattern.compile("^(print|while|if|int|string|boolean|true|false)");
        identifier = Pattern.compile("^[a-z]");
        symbol = Pattern.compile("^(\\{|\\}|\\(|\\)|==|!=|\\+)");
        digit = Pattern.compile("^[0-9]+");
        character = Pattern.compile("^[a-z]");
        */

        explicitIdentifier = Pattern.compile("[^pwisbtf]");
        keyword = Pattern.compile("print|while|if|int|string|boolean|true|false");
        identifier = Pattern.compile("[a-z]");
        symbol = Pattern.compile("\\{|\\}|\\(|\\)|==|!=|\\+");
        digit = Pattern.compile("[0-9]");
        character = Pattern.compile("[a-z]|\\s");
        quote = Pattern.compile("\"");

        boundry = Pattern.compile("\\s|\\n|\\t|\\r|\\f");
    }

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
            return "Boundry";
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