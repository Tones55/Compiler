import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SemanticAnalysis {

    private static boolean verbose = true;
    private static boolean hasError = false;
    private static DefaultMutableTreeNode cst;
    private static DefaultMutableTreeNode ast;
    private static DefaultMutableTreeNode symbolTable;
    private static Enumeration<TreeNode> cstEnumeration;
    private static Enumeration<TreeNode> astEnumeration;
    private static DefaultMutableTreeNode currentCSTNode;
    private static DefaultMutableTreeNode currentASTNode;
    private static DefaultMutableTreeNode currentSymbolTableNode;
    private static ArrayList<DefaultMutableTreeNode> blockBackTrackNodes = new ArrayList<DefaultMutableTreeNode>();

    public static String doSemanticAnalysis(DefaultMutableTreeNode root) {
        if (root == null) {
            hasError = true;
        }

        if (!hasError) {
            System.out.println("Semantic Analysis: Doing Semantic Analysis...");

            cst = root;
            cstEnumeration = cst.preorderEnumeration();
            skipCSTNodes(2);

            ast = new DefaultMutableTreeNode(currentCSTNode.toString());
            currentASTNode = ast;
            blockBackTrackNodes.add(ast);
            if (verbose) {System.out.println("Semantic Analysis: Added: " + currentCSTNode.toString().split(" ")[0]);}

            generateAST();
            System.out.println(); // For formatting

            astEnumeration = ast.preorderEnumeration();
            symbolTable = new DefaultMutableTreeNode(new Hashtable<String , VariableInfo>());
            currentASTNode = (DefaultMutableTreeNode) astEnumeration.nextElement();
            currentSymbolTableNode = symbolTable;

            //generateSymbolTable();

            printAST();
        }
        else {
            System.out.println("Semantic Analysis: Skipping Semantic Analysis due to Parse Error");
        }
        return "Semantic Analysis";
    }

    /* 
        The following code is for the AST 
    */

    private static void printAST() {
        System.out.println("\nSemantic Analysis: Printing AST...");
        Enumeration<TreeNode> astE = ast.preorderEnumeration();

        while (astE.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) astE.nextElement();
            for (int i = 0; i < node.getLevel(); i++) {
                System.out.print("-");
            }
            System.out.println(((String) node.getUserObject()).split(" ")[0]);
        }
        System.out.println();
    }

    private static void generateAST() {
        while (cstEnumeration.hasMoreElements()) {
            skipCSTNodes(1);
            switch (currentCSTNode.toString().split(" ")[0]) {
                case "<Block>":
                    addBlock();
                    break;
                case "}":
                    if (blockBackTrackNodes.size() > 0) {
                        currentASTNode = blockBackTrackNodes.get(blockBackTrackNodes.size() - 1);
                        blockBackTrackNodes.remove(blockBackTrackNodes.size() - 1);
                    }
                    else {
                        System.out.println("Semantic Analysis: Something went wrong with the AST");
                    }
                    break;
                case "<Print_Statement>":
                    addPrintStatement();
                    break;
                case "<Assignment_Statement>":
                    addAssignmentStatement();
                    break;
                case "<Variable_Decleration>":
                    addVariableDecleration();
                    break;
                case "<While_Statement>":
                    addWhileStatement();
                    break;
                case "<If_Statement>":
                    addIfStatement();
                    break;
            }
        }
    }

    private static void addBlock() {
        blockBackTrackNodes.add((DefaultMutableTreeNode) currentASTNode.getParent());
        addASTNode();
    }
    
    private static void addPrintStatement() {
        addASTNode();
        DefaultMutableTreeNode backTrackNode = (DefaultMutableTreeNode) currentASTNode.getParent();
        skipCSTNodes(4);
        decipherExpression();
        currentASTNode = backTrackNode;
    }
    
    private static void addAssignmentStatement() {
        addASTNode();
        DefaultMutableTreeNode backTrackNode = (DefaultMutableTreeNode) currentASTNode.getParent();
        skipCSTNodes(1);
        addIdentifier();
        skipCSTNodes(2);
        decipherExpression();
        currentASTNode = backTrackNode;
    }
    
    private static void addVariableDecleration() {
        addASTNode();
        DefaultMutableTreeNode backTrackNode = (DefaultMutableTreeNode) currentASTNode.getParent();
        skipCSTNodes(2);
        addASTNode();
        currentASTNode = (DefaultMutableTreeNode) currentASTNode.getParent();
        skipCSTNodes(3);
        addASTNode();
        currentASTNode = backTrackNode;
    }
    
    private static void addWhileStatement() {
        addASTNode();
        skipCSTNodes(2);
        findBooleanType();
        addBlock();
    }
    
    private static void addIfStatement() {
        addASTNode();
        skipCSTNodes(2);
        findBooleanType();
        addBlock();
    }
    
    private static void addStringExpression() {
        String str = "\"";
        skipCSTNodes(4);
        while (!currentCSTNode.toString().split(" ")[0].equals("\"")) {

            if(Character.isLetter(currentCSTNode.toString().split(" ")[0].charAt(0)) && currentCSTNode.toString().split(" ")[0].length() == 1) {
                str += currentCSTNode.toString().split(" ")[0];
            }
            else if (currentCSTNode.toString().split(" ")[0].equals("SPACE")) {
                str += "_";
            }
            skipCSTNodes(1);
        }
        str += "\"";
        DefaultMutableTreeNode strNode = new DefaultMutableTreeNode(str);
        currentASTNode.add(strNode);
        currentASTNode = (DefaultMutableTreeNode) currentASTNode.getParent();
    }
    
    private static void addIntegerExpression() {
        DefaultMutableTreeNode backTrackNode;
        DefaultMutableTreeNode digitNode;
        skipCSTNodes(2);
        digitNode = new DefaultMutableTreeNode(currentCSTNode.toString().split(" ")[0]);
        skipCSTNodes(1);
        if(currentCSTNode.toString().split(" ")[0].equals("<Integer_Operator>")) {
            DefaultMutableTreeNode sumNode = new DefaultMutableTreeNode("<Sum_Of>");
            currentASTNode.add(sumNode);
            currentASTNode = sumNode;
            backTrackNode = (DefaultMutableTreeNode) currentASTNode.getParent();
            currentASTNode.add(digitNode);
            skipCSTNodes(3);
            decipherExpression();
            currentASTNode = backTrackNode;
        }
        else {
            currentASTNode.add(digitNode);
        }
        
    }
    
    private static void addBooleanExpression() {
        DefaultMutableTreeNode tempNode = new DefaultMutableTreeNode("temp");
        currentASTNode.add(tempNode);
        currentASTNode = tempNode;
        DefaultMutableTreeNode backTrackNode = (DefaultMutableTreeNode) currentASTNode.getParent();
        skipCSTNodes(2);
        decipherExpression();
        skipCSTNodes(1);
        if(currentCSTNode.toString().split(" ")[0].equals("==")) {
            currentASTNode.setUserObject("<Is_Equal>");
        }
        else if(currentCSTNode.toString().split(" ")[0].equals("!=")) {
            currentASTNode.setUserObject("<Is_Not_Equal>");
        }
        else {
            System.out.println("\nSemantic Analysis: Error: Invalid Boolean Operator\n");
        }
        skipCSTNodes(2);
        decipherExpression();
        skipCSTNodes(1);
        currentASTNode = backTrackNode;
    }
    
    private static void addBooleanValue() {
        skipCSTNodes(1);
        addASTNode();
        skipCSTNodes(1);
        currentASTNode = (DefaultMutableTreeNode) currentASTNode.getParent();
    }
    
    private static void addIdentifier() {
        skipCSTNodes(2);
        addASTNode();
        skipCSTNodes(1);
        currentASTNode = (DefaultMutableTreeNode) currentASTNode.getParent();
    }
    // this method will not add nodes to the AST
    // it just figures out which expression method to call
    private static void decipherExpression() {
        switch (currentCSTNode.toString().split(" ")[0]) {
            case "<Int_Expression>":
                addIntegerExpression();
                break;
            case "<String_Expression>":
                addStringExpression();
                break;
            case "<Boolean_Expression>":
                findBooleanType();
                break;
            case "<Id>":
                addIdentifier();
                break;
        }
    }
   
    private static void findBooleanType() {
        skipCSTNodes(1);
        switch (currentCSTNode.toString().split(" ")[0]) {
            case "(":
                addBooleanExpression();
                break;
            case "<Boolean_Value>":
                addBooleanValue();
                break;
        }
    }
    // used to skip num nodes in the CST
    private static void skipCSTNodes(int num) {
        for (int i = 0; i < num; i++) {
            currentCSTNode = (DefaultMutableTreeNode) cstEnumeration.nextElement();
        }
    }
    //adds a node to the AST
    private static void addASTNode() {
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(currentCSTNode.toString().split(" ")[0]);
        currentASTNode.add(newNode);
        currentASTNode = newNode;
        if (verbose) {System.out.println("Semantic Analysis: Added to AST: " + currentCSTNode.toString().split(" ")[0]);}
    }
   
    /* 
        The following code is for the Symbol Table 
    */

    private static void generateSymbolTable() {
        while(astEnumeration.hasMoreElements()) {
            nextASTNode();
            switch (currentASTNode.toString()) {
                case "<Block>":
                    addSymbolTableNode();
                    break;
                case "<Print_Statement>":
                    checkPrintStatement();
                    break;
                case "<Assignment_Statement>":
                    checkAssignmentStatement();
                    break;
                case "<Variable_Decleration>":
                    VariableDecleration();
                    break;
                case "<While_Statement>":
                    checkWhileStatement();
                    break;
                case "<If_Statement>":
                    checkIfStatement();
                    break;
                case "<String_Expression>":
                    checkStringExpression();
                    break;
            }
        }
    }
    // create a new scope when a block is found
    private static void addSymbolTableNode() {
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode();
        currentSymbolTableNode.add(newNode);
        currentSymbolTableNode = newNode;
        if (verbose) {System.out.println("Added new scope" );}
    }

    private static void checkPrintStatement() {
        boolean isValid;

        nextASTNode();
        if (Character.isLetter(currentASTNode.getUserObject().toString().charAt(0))) {

            isValid = checkVariableScope();
            if (!isValid) {
                System.out.println("Semantic Analysis: Error: Variable " + currentASTNode.toString() + " is not in scope");
            }

            isValid = checkVariableInitialization();
            if (!isValid) {
                System.out.println("Semantic Analysis: Error: Variable " + currentASTNode.toString() + " is not initialized");
            }

            // mark variable as used
        }
        else {
            // nothing to check
        }
    }

    private static void checkAssignmentStatement() {
        boolean isValid;

        nextASTNode();
        isValid = checkVariableScope();
        if (!isValid) {
            System.out.println("Semantic Analysis: Error: Variable " + currentASTNode.toString() + " is not in scope");
        }

        isValid = checkVariableType();
        if (!isValid) {
            System.out.println("Semantic Analysis: Error: Variable " + currentASTNode.toString() + " is not of type " + currentASTNode.toString());
        }

        // mark variable as initialized
    }

    private static void VariableDecleration() {
        boolean isValid;

        isValid = checkCurrentScope();
        if (!isValid) {
            System.out.println("Semantic Analysis: Error: Variable " + currentASTNode.toString() + " has already been declared in this scope");
        }
        else {
            addToHashTable();
        }
    }

    private static void checkWhileStatement() {
        if (checkBooleanExpression()) {
            // nothing to check
        }
        else {
            // add all nodes to list until <Block> is found starting with first operand (no <>)
            // iterate through list and check all variables are in scope and initialized / change the list to just be types
            // iterate through the list and check all comparisons are of the same type comparisons are related to index (0-1 , 2-3 , 4-5 , 6-7...)
            boolean isValid;
            ArrayList<String> operands = new ArrayList<String>();
            DefaultMutableTreeNode currentScope = currentSymbolTableNode;
            nextASTNode();

            do {
                operands.add(currentASTNode.toString());
                nextASTNode();
            } while (currentASTNode.toString() != "<Block>");

            for (int i = 0; i < operands.size(); i++) {
                if (Character.isLetter(operands.get(i).charAt(0))) {

                    isValid = checkVariableScope();
                    if (!isValid) {
                        System.out.println("Semantic Analysis: Error: Variable " + operands.get(i) + " is not in scope");
                    }

                    isValid = checkVariableInitialization();
                    if (!isValid) {
                        System.out.println("Semantic Analysis: Error: Variable " + operands.get(i) + " is not initialized");
                    }

                    // replace variable with its type in the list
                    operands.set(i, getVariableType(currentScope, operands.get(i)));
                }
                else {
                    // replace the operand with its type in the list
                    operands.set(i, getDataType(operands.get(i)));
                }
            }

            for (int i = 0; i < operands.size(); i += 2) {
                if (!(operands.get(i).equals(operands.get(i + 1)))) {
                    System.out.println("Semantic Analysis: Error: Cannot compare type: " + operands.get(i) + " with type: " + operands.get(i + 1));
                }
            }
        }
        addSymbolTableNode();
    }

    private static void checkIfStatement() {
        // same as while
    }

    private static void checkStringExpression() {
        
    }

    private static boolean checkVariableScope() {
        // mark variable as used
        return false;
    }

    private static boolean checkCurrentScope() {
        return false;
    }

    private static boolean checkVariableType() {
        return false;
    }

    private static boolean checkVariableInitialization() {
        return false;
    }

    private static boolean checkVariableUsed() {
        return false;
    }

    private static String getVariableType(DefaultMutableTreeNode scope , String variable) {
        return "";
    }

    private static String getDataType(String data) {
        String type;
        switch (data.charAt(0)) {
            case '<':
                type = "boolean";
                break;
            case '"':
                type = "string";
                break;
            default:
                type = "int";
                break;
        }
        return type;
    }
    // returns true for boolval and false for a comparison
    private static boolean checkBooleanExpression() {
        if (currentASTNode.toString().equals("true") || currentASTNode.toString().equals("false")) {
            return true;
        }
        else {
            return false;
        }
    }

    private static void nextASTNode() {
        currentASTNode = (DefaultMutableTreeNode) astEnumeration.nextElement();
    }

    private static void addToHashTable() {

    }
}
