package src;

import java.util.ArrayList;

public class Lexer {

    private static boolean verbose = false;
    private static ArrayList<String> program;
    private static ArrayList<Token> tokens;
    private static Token lastToken;
    private static Position lastPosition;
    private static Position currentPosition;
    private static boolean isString;

    public static ArrayList<Token> doLex(ArrayList<String> prog){
        program = prog;

        tokens = new ArrayList<Token>();
        lastToken = null;
        lastPosition = new Position(1,1);
        currentPosition = new Position(1,0);
        isString = false;

        lex(program.get(0));
        System.out.println("\n");
        return tokens;
    }

    private static void lex(String currentWork){
        if(verbose) System.out.println(currentWork);
        
        String visible = ""; // the part of current work the lexer is currently looking at
        String match = ""; // the type of match found for the visible string
        String currentChar = ""; // the current character being looked at
        String currentMatch = ""; // the type of match found for the current character

        for(int i=0; i<currentWork.length(); i++){

            //increment current position column
            currentPosition.setColumn(currentPosition.getColumn() + 1);

            currentChar = currentWork.charAt(i) + "";
            visible += currentWork.charAt(i);

            // if a quote was found lex for a string
            if(!isString){
                match = PatternMatcher.match(visible, lastPosition , 1);
                currentMatch = PatternMatcher.match(currentChar , currentPosition , 2);

                if (currentMatch.equals("Illegal Character")) {
                    System.out.println("Lex Error: Illegal Character: " + currentChar + " at position: " + currentPosition);
                    return;
                }

                //if there is 1 char in visible check for 1 char matches
                if (visible.length() == 1) {
                    // if the current character is a boundry
                    if (match.equals("Boundry")) {
                        // if the single char is a boundry, skip it
                        visible = "";
                        currentWork = currentWork.substring(i + 1);
                        i = -1;
                        continue;
                    }
                    else if (match.equals("Quote")) {
                        // if the single char is a quote, start lexing for a string
                        isString = true;
                        tokens.add(lastToken);
                        System.out.println(lastToken.toString());
                        currentWork = currentWork.substring(i + 1);
                        i = -1;
                        continue;
                    }
                    else if (match.equals("Digit")) {
                        // if the single char is a digit, add it to the token list
                        if(lastToken != null){
                            /*tokens.add(lastToken); in clean up*/
                            break;
                        }
                    }
                    else if (match.equals("Symbol")) {
                        // if the single char is a symbol, add it to the token list
                        if(lastToken != null){
                            /*tokens.add(lastToken); in clean up*/
                            break;
                        }
                    }
                    else if (match.equals("Explicit Identifier")) {
                        // if the single char is a explicit identifier, add it to the token list
                        if(lastToken != null){
                            /*tokens.add(lastToken); in clean up*/
                            break;
                        }
                    }
                }
                else {
                    if (currentMatch.equals("Boundry") | currentMatch.equals("Quote") | 
                    currentMatch.equals("symbol") | currentMatch.equals("Assign")) {
                        // if a token was found add the token and go to "Clean up"
                        if(lastToken != null){
                            /*tokens.add(lastToken); in clean up*/
                            break;
                        }
                        // if no token was found and a boundry was hit, report error
                        else {
                            System.out.println("Lex Error: Unknown token on line: " + lastPosition.getLine() +
                                " near column: " + lastPosition.getColumn());
                        }
                    }
                    if (currentMatch.equals("")) {
                    }
                }
            }
            // if in a string do this to lex instead
            // do not exit loop for duration of string, this improves preformace 
            else {
                currentMatch = PatternMatcher.checkForCharList(currentChar, currentPosition);
                // if the string contains a legal character
                if (currentMatch.equals("Character")) {
                    tokens.add(lastToken);
                    System.out.println(lastToken.toString());
                }
                // if end quote was found
                else if (currentMatch.equals("Quote")) {
                    tokens.add(lastToken);
                    System.out.println(lastToken.toString());
                    isString = false;
                    currentWork = currentWork.substring(i + 1);
                    i = 0;
                }
                else {
                    System.out.println("Lex Error: Cannot use character \"" + currentChar + "\" inside of a string" + " found at " + currentPosition.toString());
                    return;
                }
            }
        }

        // "Clean up"
        // prepares for the next string to be lexed, throws and error, or proceeds to Parse

        // if a token was found shorten the working string and add the token to the list
        if (lastToken != null && !lastToken.getName().equals("Quote")) {
            currentWork = currentWork.substring(lastToken.getValue().length());
            lastPosition.setColumn(currentPosition.getColumn());
            tokens.add(lastToken);
            System.out.println(lastToken.toString());
        }

        // if the string is not empty
        if(currentWork.length() > 0){
            // if there was a token found
            if (lastToken != null) {
                lastToken = null;
                lex(currentWork);
                return;
            }
            else if (match.equals("End of Program")) {
                lastToken = new Token("End of Program", "$", currentPosition);
                System.out.println(lastToken.toString());
                return;
            }
            // if there was no token found but there is still a string to lex
            else {
                System.out.println("Lex Error: Unknown token on line: " + lastPosition.getLine() +
                " near column: " + lastPosition.getColumn());
                return;
            }
        }
        // if the string is empty and there is a new line to lex
        else if (currentPosition.getLine() < program.size()) {
            if (lastToken != null) {
                lastToken = null;
            }
            // start lexing the next line
            currentWork = program.get(currentPosition.getLine());
            incrementLine();
            lex(currentWork);
            return;
        }
        else { return; } // if the string is empty and there is no new line to lex
    }

    public static void updateToken(String type, String value, Position position , int t){
        if (t == 1) {
            lastToken = new Token(type, value, position);
        }
    }

    public static void incrementLine() {
        lastPosition.setLine(lastPosition.getLine() + 1); 
        lastPosition.setColumn(1);
        currentPosition.setLine(currentPosition.getLine() + 1);
        currentPosition.setColumn(1);
        Compiler.fileLine++;
    }
}