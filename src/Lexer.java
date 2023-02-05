package src;

import java.util.ArrayList;

public class Lexer {

    private boolean verbose = true;
    private ArrayList<String> program;
    private boolean isString = false;
    private static ArrayList<Token> tokens = new ArrayList<Token>();
    private static Token lastToken = null;
    private Position position;
    private Position boundry = null;

    public Lexer(ArrayList<String> program){
        this.program = program;

        position = new Position(1,1);
        for(int i=0; i<program.size(); i++) {
            lex(program.get(i));
        }
    }

    private void lex(String currentWork){
        if(verbose) System.out.println(currentWork);
        
        String visible = "";
        for(int i=0; i<currentWork.length(); i++){
            visible += currentWork.charAt(i);
            PatternMatcher.match(visible, position);
        }
    }

    public static void createtoken(String type, String value, Position position){
        lastToken = new Token(type, value, position);
    }
}