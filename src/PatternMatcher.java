package src;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

public class PatternMatcher {

    private static Pattern keyword;
    private static Pattern identifier;
    private static Pattern symbol;
    private static Pattern digit;
    private static Pattern character;
    private static Pattern comment;

    //used to aid in the creation of tokens but are not tokens themselves
    private static Pattern boundry;
    private static Pattern eof;
    
    public PatternMatcher(){
        /*
        keyword = Pattern.compile("^(print|while|if|int|string|boolean|true|false)");
        identifier = Pattern.compile("^[a-z]");
        symbol = Pattern.compile("^(\\{|\\}|\\(|\\)|==|!=|\\+)");
        digit = Pattern.compile("^[0-9]+");
        character = Pattern.compile("^[a-z]");
        */

        keyword = Pattern.compile("print|while|if|int|string|boolean|true|false");
        identifier = Pattern.compile("[a-z]");
        symbol = Pattern.compile("\\{|\\}|\\(|\\)|==|!=|\\+");
        digit = Pattern.compile("[0-9]");
        character = Pattern.compile("[a-z]|\\s");
        comment = Pattern.compile("(/\\*)((a-z)*)(\\*/)");

        boundry = Pattern.compile("\\s|\\n|\\t|\\r|\\f");
        eof = Pattern.compile("$");


    }

    public static void match(String input , Position pos){
        Matcher m = keyword.matcher(input);
        if(m.find()){
            Lexer.createtoken("Keyword" , input , pos);
        }
        m = identifier.matcher(input);
        if(m.find()){
            Lexer.createtoken("Identifier" , input , pos);
        }
        m = symbol.matcher(input);
        if(m.find()){
            Lexer.createtoken("Symbol" , input , pos);
        }
        m = digit.matcher(input);
        if(m.find()){
            Lexer.createtoken("Digit" , input , pos);
        }
        m = character.matcher(input);
        if(m.find()){
            Lexer.createtoken("Character" , input , pos);
        }
        m = comment.matcher(input);
        if(m.find()){
            Lexer.createtoken("Comment" , input , pos);
        }

        m = boundry.matcher(input);
        if(m.find()){
            
        }
        m = eof.matcher(input);
        if(m.find()){
            
        }
    }
}