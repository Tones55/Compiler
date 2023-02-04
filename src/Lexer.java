package src;

import java.util.ArrayList;

public class Lexer {

    private boolean verbose = true;
    private String program;
    private boolean isString = false;
    private ArrayList<Token> tokens = new ArrayList<Token>();
    private Token lastToken = null;
    private int line = 1;
    private int column = 1;


    public Lexer(String program , PatternMatcher patternMatcher){
        this.program = program;
        lex();
    }

    private void lex(){
        if(verbose) printProgram();
    }

    private void printProgram(){
        System.out.println("Program: " + program);
    }
}