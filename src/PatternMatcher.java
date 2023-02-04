package src;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

public class PatternMatcher {

    Pattern keyword;
    Pattern identifier;
    Pattern symbol;
    Pattern digit;
    Pattern character;
    
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
        symbol = Pattern.compile("{|}|(|)|==|!=|+");
        digit = Pattern.compile("[0-9]");
        character = Pattern.compile("[a-z]|\\s");
    }

    public String match(String input){
        Matcher m = keyword.matcher(input);
        if(m.find()){
            return "keyword";
        }
        m = identifier.matcher(input);
        if(m.find()){
            return "identifier";
        }
        m = symbol.matcher(input);
        if(m.find()){
            return "symbol";
        }
        m = digit.matcher(input);
        if(m.find()){
            return "digit";
        }
        m = character.matcher(input);
        if(m.find()){
            return "character";
        }
        return "error";
    }
}