import java.util.ArrayList;

public class Lexer {

    private static boolean verbose = true;
    private static ArrayList<String> program;
    private static ArrayList<Token> tokens;
    private static Token lastToken;
    private static Position lastPosition;
    private static Position currentPosition;
    private static boolean isString;
    private static boolean hasError;

    public static ArrayList<Token> doLex(ArrayList<String> prog){
        program = prog;

        hasError = false;
        tokens = new ArrayList<Token>();
        lastToken = null;
        lastPosition = new Position(1,1);
        currentPosition = new Position(1,0);
        isString = false;

        lex(program.get(0));
        if (verbose) { System.out.println("\n");}
        if (hasError) {
            tokens = null;
        }
        return tokens;
    }

    private static void lex(String currentWork){
        
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
                match = PatternMatcher.match(visible, 1);
                currentMatch = PatternMatcher.match(currentChar , 2);

                if (currentMatch.equals("Illegal Character")) {
                    System.out.println("Input Line: " + Compiler.fileLine + " :: Lex Error: Illegal Character: " +
                     currentChar + " at position: " + lastPosition);
                    System.out.println("\t Legal characters are: a-z 0-9 'space' 'tab' 'return' { } ( ) = ! + \" $");
                    hasError = true;
                    return;
                }

                //if there is 1 char in visible check for 1 char matches
                if (visible.length() == 1) {
                    // if the current character is a boundry
                    if (match.equals("Boundry")) {
                        // if the single char is a boundry, skip it
                        visible = "";
                        currentWork = currentWork.substring(i + 1);
                        lastPosition.setColumn(lastPosition.getColumn() + 1);
                        lex(currentWork);
                        return;
                    }
                    else if (match.equals("Quote")) {
                        // if the single char is a quote, start lexing for a string
                        isString = true;
                        break;
                    }
                    else if (match.equals("Digit")) {
                        // if the single char is a digit, add it to the token list
                        break;
                    }
                    else if (match.equals("Symbol")) {
                        // if the single char is a symbol, add it to the token list
                        break;
                    }
                    else if (match.equals("Explicit Identifier")) {
                        // if the single char is a explicit identifier, add it to the token list
                        break;
                    }
                    if (currentChar.equals("!")) {
                        if (currentWork.length() > i) {
                            if (currentWork.charAt(i + 1) != '=') {
                                System.out.println("Input Line: " + Compiler.fileLine + " :: Lex Error: found \"!\" with no \"=\" following it. a \"!\" cannot be used in this way: " + 
                                (currentPosition.getLine()-1) + " near column: " + currentPosition.getColumn());
                                return;
                            }
                        }
                        else {
                            System.out.println("Input Line: " + Compiler.fileLine + " :: Lex Error: found \"!\" with no \"=\" following it. a \"!\" cannot be used in this way: " + 
                                (currentPosition.getLine()-1) + " near column: " + currentPosition.getColumn());
                                return;
                        }
                    }
                }
                else {
                    if (currentMatch.equals("Boundry") | currentMatch.equals("Quote") | 
                    currentMatch.equals("symbol") | currentMatch.equals("Assign")) {
                        // if a token was found add the token and go to "Clean up"
                        if(lastToken != null){
                            break;
                        }
                        // if no token was found and a boundry was hit, report error
                        else {
                            System.out.println("Input Line: " + Compiler.fileLine + " :: Lex Error: Unknown token on line: " + lastPosition.getLine() +
                                " near column: " + lastPosition.getColumn());
                            System.out.println("Not sure how this even happened but it did");
                            hasError = true;
                            return;
                        }
                    }
                }
            }
            // if in a string do this to lex instead
            else {
                
                currentMatch = PatternMatcher.checkForCharList(currentChar);

                // if the string contains a legal character
                if (currentMatch.equals("Character")) {
                    if (i == currentWork.length() - 1) {
                        System.out.println("Input Line: " + Compiler.fileLine + " :: Lex Error: string is never terminated, found on line: " + 
                        (currentPosition.getLine()-1) + " near column: " + currentPosition.getColumn());
                        hasError = true;
                        return;
                    }
                    else {
                        break;
                    }
                }
                // if end quote was found
                else if (currentMatch.equals("Quote")) {
                    isString = false;
                    break;
                }
                else {
                    System.out.println("Input Line: " + Compiler.fileLine + " :: Lex Error: Cannot use character \"" + currentChar +
                     "\" inside of a string" + " found at " + currentPosition);
                    System.out.println("\t Legal characters are: a-z 'space'");
                    hasError = true;
                    return;
                }
            }
        }

        // "Clean up"
        // prepares for the next string to be lexed, adds tokens, throws errors, or proceeds to Parse

        // if a token was found shorten the working string and add the token to the list
        if (lastToken != null) {
            currentWork = currentWork.substring(lastToken.getValue().length());
            tokens.add(lastToken);
            lastPosition.setColumn(lastPosition.getColumn() + lastToken.getValue().length());
            printToken();
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
                lastToken = new Token("End of Program", "$", new Position(currentPosition.getLine(), currentPosition.getColumn()));
                printToken();
                return;
            }
            // if there was no token found but there is still a string to lex
            else {
                System.out.println("Input Line: " + Compiler.fileLine + " :: Lex Error: Unknown token on line: " + lastPosition.getLine() +
                " near column: " + lastPosition.getColumn());
                System.out.println("Not sure how this even happened but it did");
                hasError = true;
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

    public static void updateToken(String type, String value, int t){
        if (t == 1) {
            lastToken = new Token(type, value, new Position(lastPosition.getLine(), lastPosition.getColumn()));
        }
    }

    public static void incrementLine() {
        lastPosition.setLine(lastPosition.getLine() + 1); 
        lastPosition.setColumn(1);
        currentPosition.setLine(currentPosition.getLine() + 1);
        currentPosition.setColumn(0);
        Compiler.fileLine++;
    }

    public static void printToken() {
        if (verbose) {
            System.out.println(lastToken.toString());
        }
    }
}