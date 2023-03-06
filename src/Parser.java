import java.util.ArrayList;
import javax.swing.tree.DefaultMutableTreeNode;

public class Parser {

    private static boolean verbose = true;
    private static boolean hasError;
    private static ArrayList<Token> tokens;
    private static int tokenIndex;
    private static DefaultMutableTreeNode root;
    private static DefaultMutableTreeNode currentNode;
    
    public static DefaultMutableTreeNode doParse (ArrayList<Token> lexTokens) {

        if (tokens == null) {
            hasError = true;
        }
        if (!hasError) {
            tokens = lexTokens;
            tokenIndex = 0;
            parse();
            if (verbose) { System.out.println("\n");}
        }
        else {
            root = null;
        }
        return root;
    }

    private static void parse() {
        parseProgram();
    }

    private static void match(String expected) {
        if (tokens.get(tokenIndex).getValue().equals(expected)) {
            addNode(expected);
            tokenIndex++;
        }
        else {
            //error
        }
    }

    private static void addNode (String name) {
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new Node(name, tokens.get(tokenIndex)));
        currentNode.add(newNode);
        currentNode = newNode;
    }

    private static void addNode (String name , int isRoot) {
           root = new DefaultMutableTreeNode(new Node(name, tokens.get(tokenIndex)));
           currentNode = root;
    }

    private static void parseProgram () {
        addNode("Program" , 1);
        parseBlock();
        match("");
    }

    private static void parseBlock () {
        addNode("Block");
        match("{");
        parseStatementList();
        match("}");
    }

    private static void parseStatementList () {
        addNode("StatementList");
        if (tokens.size() == tokenIndex + 1) {
            //error
        }
        else if (tokens.get(tokenIndex+1).getValue().equals("}") ) {
            return;
        }
        else {
            parseStatement();
            parseStatementList();
        }
    }

    private static void parseStatement () {

    }

    private static void parsePrintStatement () {

    }

    private static void parseAssignmentStatement () {

    }

    private static void parseVariableDeclaration () {

    }

    private static void parseWhileStatement () {

    }

    private static void parseIfStatement () {

    }

    private static void parseExpression () {

    }

    private static void parseIntExpression () {

    }

    private static void parseStringExpression () {

    }

    private static void parseBooleanExpression () {

    }

    private static void parseId () {

    }

    private static void parseCharList () {

    }

    private static void parseType () {

    }

    private static void parseChar () {

    }

    private static void parseSpace () {

    }

    private static void parseDigit () {

    }

    private static void parseBooleanOperator () {

    }

    private static void parseBooleanValue () {

    }

    private static void parseIntegerOperator () {

    }
}
