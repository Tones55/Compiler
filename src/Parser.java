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

    private static void moveUp() {
        currentNode = (DefaultMutableTreeNode) currentNode.getParent();
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

    private static void addNode (String name , boolean noToken) {
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new Node(name, null));
        currentNode.add(newNode);
        currentNode = newNode;
    }

    private static void parseProgram () {
        addNode("Program" , 1);
        parseBlock();
        match("$");
    }

    private static void parseBlock () {
        addNode("Block" , true);
        match("{");
        parseStatementList();
        match("}");
        moveUp();
    }

    private static void parseStatementList () {
        addNode("Statement List" , true);
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
        moveUp();
    }

    private static void parseStatement () {

    }

    private static void parsePrintStatement () {
        addNode("Print Statement" , true);
        match("print");
        match("(");
        parseStatement();
        match(")");
        moveUp();
    }

    private static void parseAssignmentStatement () {
        addNode("Assignment Statement" , true);
        parseId();
        match("=");
        parseExpression();
        moveUp();
    }

    private static void parseVariableDeclaration () {
        addNode("Variable Decleration" , true);
        parseType();
        parseId();
        moveUp();
    }

    private static void parseWhileStatement () {
        addNode("While Statement" , true);
        match("while");
        parseBooleanExpression();
        parseBlock();
        moveUp();
    }

    private static void parseIfStatement () {
        addNode("If Statement" , true);
        match("if");
        parseBooleanExpression();
        parseBlock();
        moveUp();
    }

    private static void parseExpression () {

    }

    private static void parseIntExpression () {
        addNode("Int Expression" , true);
        parseDigit();
        if (tokens.size() < tokenIndex + 1) {
            //error
        }
        else {
            if (tokens.get(tokenIndex + 1).getValue().equals("+")) {
                parseIntegerOperator();
                parseExpression();
            }
        }
        moveUp();
    }

    private static void parseStringExpression () {
        addNode("String Expression" , true);
        match("\"");
        parseCharList();
        match("\"");
        moveUp();
    }

    private static void parseBooleanExpression () {
        addNode("Boolean Expression" , true);
        if (tokens.get(tokenIndex).getValue().equals("(")) {
            match("(");
            parseExpression();
            parseBooleanOperator();
            parseExpression();
            match(")");
        }
        else {
            parseBooleanValue();
        }
        moveUp();
    }

    private static void parseId () {
        addNode("Id");
        parseChar();
        moveUp();
    }

    private static void parseCharList () {
        addNode("Char List" , true);
        if (!tokens.get(tokenIndex+1).getValue().equals("\"")) {
            if (!tokens.get(tokenIndex).getValue().equals(" ")) {
                parseChar();
                parseCharList();
            }
            else {
                parseSpace();
                parseCharList();
            }
        }
        else {
            //nothing
        }
        moveUp();
    }

    private static void parseType () {
        addNode("Type" , true);
        if (tokens.get(tokenIndex).getValue().equals("int")) {
            match("int");
        }
        else if (tokens.get(tokenIndex).getValue().equals("string")) {
            match("string");
        }
        else {
            match("boolean");
        }
        moveUp();
    }

    private static void parseChar () {
        addNode("Char" , true);

    }

    private static void parseSpace () {
        addNode("Space" , true);
        match(" ");
        moveUp();
    }

    private static void parseDigit () {
        addNode("Digit" , true);
        //help

    }

    private static void parseBooleanOperator () {
        addNode("Boolean Operator" , true);
        if (tokens.get(tokenIndex).getValue().equals("==")) {
            match("==");
        }
        else {
            match("!=");
        }
        moveUp();
    }

    private static void parseBooleanValue () {
        addNode("Boolean Value" , true);
        if (tokens.get(tokenIndex).getValue().equals("true")) {
            match("true");
        }
        else {
            match("false");
        }
        moveUp();
    }

    private static void parseIntegerOperator () {
        addNode("Integer Operator" , true);
        match("+");
        moveUp();
    }
}
