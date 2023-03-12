import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultMutableTreeNode;

public class Parser {

    private static boolean verbose = true;
    private static boolean hasError;
    private static ArrayList<Token> tokens;
    private static int tokenIndex;
    private static DefaultMutableTreeNode root;
    private static DefaultMutableTreeNode currentNode;
    private static DefaultMutableTreeNode displayableTreeRoot;
    private static DefaultMutableTreeNode displayableTreeCurrentNode;
    
    public static DefaultMutableTreeNode doParse (ArrayList<Token> lexTokens) {
        tokens = lexTokens;
        tokenIndex = 0;

        if (tokens == null) {
            hasError = true;
        }
        if (!hasError) {
            System.out.println("Parser: Parsing program...");
            parseProgram();
            if (verbose) { 
                System.out.println("\n");
                printCST();
            }
        }
        else {
            root = null;
        }
        return root;
    }

    private static void printCST() {
        System.out.println("Parser: Printing CST...");
        TreeGraphics.createAndShowGUI(displayableTreeRoot);

        Enumeration<TreeNode> e = root.preorderEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            for (int i = 0; i < node.getLevel(); i++) {
                System.out.print("-");
            }
            System.out.println(node.getUserObject().toString());
        }
    }

    private static void match(String expected) {
        if (tokens.get(tokenIndex).getValue().equals(expected)) {
            addNode(expected);
            if (verbose) { System.out.println("Parser: Matched: " + expected + " at position: " + tokens.get(tokenIndex).getPosition()); }
            tokenIndex++;
        }
        else {
            //error
        }
    }

    private static void match(String[] expected) {
        boolean matchfound = false;
        for (String i : expected) {
            if (tokens.get(tokenIndex).getValue().equals(i)) {
                addNode(i);
                tokenIndex++;
                matchfound = true;
                if (verbose) { System.out.println("Parser: Matched: " + i + " at position: " + tokens.get(tokenIndex).getPosition()); }
                break;
            }
        }
        if (!matchfound) {
            //error
        }
    }

    private static void moveUp() {
        currentNode = (DefaultMutableTreeNode) currentNode.getParent();
        displayableTreeCurrentNode = (DefaultMutableTreeNode) displayableTreeCurrentNode.getParent();
    }

    private static void addNode (String name) {
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new Node(name, tokens.get(tokenIndex)));
        currentNode.add(newNode);
        currentNode = newNode;
        
        DefaultMutableTreeNode newGNode = new DefaultMutableTreeNode(name);
        displayableTreeCurrentNode.add(newGNode);
        displayableTreeCurrentNode = newGNode;
    }

    private static void addNode (String name , int isRoot) {
           root = new DefaultMutableTreeNode(new Node(name, tokens.get(tokenIndex)));
           currentNode = root;

           displayableTreeRoot = new DefaultMutableTreeNode(name);
           displayableTreeCurrentNode = displayableTreeRoot;
    }

    private static void addNode (String name , boolean noToken) {
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new Node(name, null));
        currentNode.add(newNode);
        currentNode = newNode;

        DefaultMutableTreeNode newGNode = new DefaultMutableTreeNode(name);
        displayableTreeCurrentNode.add(newGNode);
        displayableTreeCurrentNode = newGNode;
    }

    private static void parseProgram () {
        if (verbose) { System.out.println("Parser: parseProgram()"); }
        addNode("Program" , 1);
        parseBlock();
        match("$");
    }

    private static void parseBlock () {
        if (verbose) { System.out.println("Parser: parseBlock()"); }
        addNode("Block" , true);
        match("{");
        parseStatementList();
        match("}");
        moveUp();
    }

    private static void parseStatementList () {
        if (verbose) { System.out.println("Parser: parseStatementList()"); }
        addNode("Statement List" , true);
        if (tokens.size() == tokenIndex + 1) {
            //error
        }
        else if (tokens.get(tokenIndex).getValue().equals("}") ) {
            //do nothing
        }
        else {
            parseStatement();
            parseStatementList();
        }
        moveUp();
    }

    private static void parseStatement () {
        if (verbose) { System.out.println("Parser: parseStatement()"); }
        addNode("Statement");
        if (tokens.get(tokenIndex).getValue().equals("print")) {
            parsePrintStatement();
        }
        else if (tokens.get(tokenIndex).getName().equals("Identifier")) {
            parseAssignmentStatement();
        }
        else if (tokens.get(tokenIndex).getValue().equals("int") || tokens.get(tokenIndex).getValue().equals("string") || tokens.get(tokenIndex).getValue().equals("boolean")) {
            parseVariableDeclaration();
        }
        else if (tokens.get(tokenIndex).getValue().equals("while")) {
            parseWhileStatement();
        }
        else if (tokens.get(tokenIndex).getValue().equals("if")) {
            parseIfStatement();
        }
        else if (tokens.get(tokenIndex).getValue().equals("{")) {
            parseBlock();
        }
        else {
            //error
        }
        moveUp();
    }

    private static void parsePrintStatement () {
        if (verbose) { System.out.println("Parser: parsePrintStatement()"); }
        addNode("Print Statement" , true);
        match("print");
        match("(");
        parseExpression();
        match(")");
        moveUp();
    }

    private static void parseAssignmentStatement () {
        if (verbose) { System.out.println("Parser: parseAssignmentStatement()"); }
        addNode("Assignment Statement" , true);
        parseId();
        match("=");
        parseExpression();
        moveUp();
    }

    private static void parseVariableDeclaration () {
        if (verbose) { System.out.println("Parser: parseVariableDeclaration()"); }
        addNode("Variable Decleration" , true);
        parseType();
        parseId();
        moveUp();
    }

    private static void parseWhileStatement () {
        if (verbose) { System.out.println("Parser: parseWhileStatement()"); }
        addNode("While Statement" , true);
        match("while");
        parseBooleanExpression();
        parseBlock();
        moveUp();
    }

    private static void parseIfStatement () {
        if (verbose) { System.out.println("Parser: parseIfStatement()"); }
        addNode("If Statement" , true);
        match("if");
        parseBooleanExpression();
        parseBlock();
        moveUp();
    }

    private static void parseExpression () {
        if (verbose) { System.out.println("Parser: parseExpression()"); }
        addNode("Expression" , true);
        if (tokens.get(tokenIndex).getName().equals("Digit")) {
            parseIntExpression();
        }
        else if (tokens.get(tokenIndex).getValue().equals("\"")) {
            parseStringExpression();
        }
        else if (tokens.get(tokenIndex).getValue().equals("true") || tokens.get(tokenIndex).getValue().equals("false") || tokens.get(tokenIndex).getValue().equals("(")) {
            parseBooleanExpression();
        }
        else if (tokens.get(tokenIndex).getName().equals("Identifier")) {
            parseId();
        }
        else {
            //error
        }
        moveUp();
    }

    private static void parseIntExpression () {
        if (verbose) { System.out.println("Parser: parseIntExpression()"); }
        addNode("Int Expression" , true);
        parseDigit();
        if (tokens.size() < tokenIndex + 1) {
            //error
        }
        else {
            if (tokens.get(tokenIndex).getValue().equals("+")) {
                parseIntegerOperator();
                parseExpression();
            }
        }
        moveUp();
    }

    private static void parseStringExpression () {
        if (verbose) { System.out.println("Parser: parseStringExpression()"); }
        addNode("String Expression" , true);
        match("\"");
        parseCharList();
        match("\"");
        moveUp();
    }

    private static void parseBooleanExpression () {
        if (verbose) { System.out.println("Parser: parseBooleanExpression()"); }
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
        if (verbose) { System.out.println("Parser: parseId()"); }
        addNode("Id");
        parseChar();
        moveUp();
    }

    private static void parseCharList () {
        if (verbose) { System.out.println("Parser: parseCharList()"); }
        addNode("Char List" , true);
        if (!tokens.get(tokenIndex).getValue().equals("\"")) {
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
        if (verbose) { System.out.println("Parser: parseType()"); }
        addNode("Type" , true);
        String[] expected = {"int" , "string" , "boolean"};
        match(expected);
        moveUp();
    }

    private static void parseChar () {
        if (verbose) { System.out.println("Parser: parseChar()"); }
        addNode("Char" , true);
        String[] expected = {"a" , "b" , "c" , "d" , "e" , "f" , "g" , "h" , "i" , "j" , "k" ,
         "l" , "m" , "n" , "o" , "p" , "q" , "r" , "s" , "t" , "u" , "v" , "w" , "x" , "y" , "z"};
        match(expected);
        moveUp();
    }

    private static void parseSpace () {
        if (verbose) { System.out.println("Parser: parseSpace()"); }
        addNode("Space" , true);
        match(" ");
        moveUp();
    }

    private static void parseDigit () {
        if (verbose) { System.out.println("Parser: parseDigit()"); }
        addNode("Digit" , true);
        String[] expected = {"0" , "1" , "2" , "3" , "4" , "5" , "6" , "7" , "8" , "9"};
        match(expected);
        moveUp();
    }

    private static void parseBooleanOperator () {
        if (verbose) { System.out.println("Parser: parseBooleanOperator()"); }
        addNode("Boolean Operator" , true);
        String[] expected = {"==" , "!="};
        match(expected);
        moveUp();
    }

    private static void parseBooleanValue () {
        if (verbose) { System.out.println("Parser: parseBooleanValue()"); }
        addNode("Boolean Value" , true);
        String[] expected = {"true" , "false"};
        match(expected);
        moveUp();
    }

    private static void parseIntegerOperator () {
        if (verbose) { System.out.println("Parser: parseIntegerOperator()"); }
        addNode("Integer Operator" , true);
        match("+");
        moveUp();
    }
}
