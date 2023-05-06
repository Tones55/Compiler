import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class SemanticAnalysis {

    private static boolean verbose = true;
    private static boolean hasError = false;
    private static DefaultMutableTreeNode cst;
    private static DefaultMutableTreeNode ast;
    private static DefaultMutableTreeNode symbolTable;
    private static DefaultMutableTreeNode currentCSTNode;
    private static DefaultMutableTreeNode currentASTNode;
    private static DefaultMutableTreeNode currentSymbolTableNode;
    private static Enumeration<TreeNode> cstEnumeration;
    private static Enumeration<TreeNode> astEnumeration;
    private static ArrayList<DefaultMutableTreeNode> blockBacktrackNodes;
    private static ArrayList<Integer> scopeBacktrackDepths;
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    public static DefaultMutableTreeNode[] doSemanticAnalysis(DefaultMutableTreeNode root) {
        initializeVariables();
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
            blockBacktrackNodes.add(ast);

            generateAST();

            System.out.println(); // for formatting

            astEnumeration = ast.preorderEnumeration();
            symbolTable = new DefaultMutableTreeNode(new Hashtable<String , VariableInfo>());
            currentASTNode = (DefaultMutableTreeNode) astEnumeration.nextElement();
            currentSymbolTableNode = symbolTable;
            scopeBacktrackDepths.add(0);

            generateSymbolTable();

            if (verbose) {
                if (!hasError) {
                    printAST();
                    printSymbolTable(1);
                    printSymbolTable();
                }
            }

            checkVariablesUsed();
        }
        else {
            System.out.println("Semantic Analysis: Skipping Semantic Analysis due to Parse Error");
        }
        if (hasError) {
            return null;
        }

        return new DefaultMutableTreeNode[] {ast, symbolTable};
    }

    private static void initializeVariables() {
        verbose = true;
        hasError = false;
        cst = null;
        ast = null;
        symbolTable = null;
        currentCSTNode = null;
        currentASTNode = null;
        currentSymbolTableNode = null;
        cstEnumeration = null;
        astEnumeration = null;
        blockBacktrackNodes = new ArrayList<DefaultMutableTreeNode>();
        scopeBacktrackDepths = new ArrayList<Integer>();
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
            if (hasError) {
                return;
            }

            skipCSTNodes(1);
            switch (currentCSTNode.toString().split(" ")[0]) {
                case "<Block>":
                    addBlock(1);
                    break;
                case "}":
                    if (blockBacktrackNodes.size() > 0) {
                        currentASTNode = blockBacktrackNodes.get(blockBacktrackNodes.size() - 1);
                        blockBacktrackNodes.remove(blockBacktrackNodes.size() - 1);
                    }
                    else {
                        // probably cant happen
                        System.out.println("Semantic Analysis: Something went wrong with the AST");
                        hasError = true;
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
        blockBacktrackNodes.add((DefaultMutableTreeNode) currentASTNode.getParent());
        while (!currentCSTNode.toString().split(" ")[0].equals("<Block>")) {
            skipCSTNodes(1);
        }
        addASTNode();
    }

    private static void addBlock(int i) {
        blockBacktrackNodes.add(currentASTNode);
        addASTNode();
    }
    
    private static void addPrintStatement() {
        addASTNode();
        DefaultMutableTreeNode backTrackNode = (DefaultMutableTreeNode) currentASTNode.getParent();
        skipCSTNodes(4);
        decipherExpression(EMPTY_STRING_ARRAY);
        currentASTNode = backTrackNode;
    }
    
    private static void addAssignmentStatement() {
        addASTNode();
        DefaultMutableTreeNode backTrackNode = (DefaultMutableTreeNode) currentASTNode.getParent();
        skipCSTNodes(1);
        addIdentifier();
        skipCSTNodes(2);
        decipherExpression(EMPTY_STRING_ARRAY);
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
        System.out.println("Semantic Analysis: Added to AST: " + strNode.toString());
        currentASTNode = (DefaultMutableTreeNode) currentASTNode.getParent();
    }
    
    private static void addIntegerExpression() {
        DefaultMutableTreeNode backTrackNode;
        DefaultMutableTreeNode digitNode;
        skipCSTNodes(2);
        digitNode = new DefaultMutableTreeNode(currentCSTNode.toString());
        skipCSTNodes(1);
        if(currentCSTNode.toString().split(" ")[0].equals("<Integer_Operator>")) {
            DefaultMutableTreeNode sumNode = new DefaultMutableTreeNode("<Sum_Of>");
            currentASTNode.add(sumNode);
            currentASTNode = sumNode;
            backTrackNode = (DefaultMutableTreeNode) currentASTNode.getParent();
            currentASTNode.add(digitNode);
            skipCSTNodes(3);
            decipherExpression(new String[] {"<Int_Expression>" , "<Id>"});
            currentASTNode = backTrackNode;
        }
        else {
            currentASTNode.add(digitNode);
        }
        
    }
    
    private static void addBooleanExpression() {
        DefaultMutableTreeNode tempNode = new DefaultMutableTreeNode("temp");
        currentASTNode.add(tempNode);
        System.out.println(currentASTNode);
        currentASTNode = tempNode;
        DefaultMutableTreeNode backTrackNode = (DefaultMutableTreeNode) currentASTNode.getParent();

        skipCSTNodes(2);
        decipherExpression(EMPTY_STRING_ARRAY);
        skipCSTNodes(1);

        while (!currentCSTNode.toString().split(" ")[0].equals("==") && !currentCSTNode.toString().split(" ")[0].equals("!=")) {
            skipCSTNodes(1);
        }
        currentASTNode = tempNode;

        if(currentCSTNode.toString().split(" ")[0].equals("==")) {
            currentASTNode.setUserObject("<Is_Equal>");
        }
        else if(currentCSTNode.toString().split(" ")[0].equals("!=")) {
            currentASTNode.setUserObject("<Is_Not_Equal>");
        }
        else {
            // probably cant happen
            System.out.println("\nSemantic Analysis: Error: Invalid Boolean Operator near line " + currentASTNode.getUserObject().toString().split(" ")[1] + "\n");
            hasError = true;
        }
        skipCSTNodes(2);
        decipherExpression(EMPTY_STRING_ARRAY);
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

    private static void decipherExpression(String[] expected) {
        // this method will not add nodes to the AST
        // it just figures out which expression method to call

        // this code up here is only important for integer expressions
        boolean isValid = true;

        for (String s : expected) {
            if (!(currentCSTNode.toString().split(" ")[0].equals(s))) {
                isValid = false;
            }
            else {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            hasError = true;
            System.out.print("Semantic Analysis: Error: Invalid Expression near line " + currentASTNode.getParent().getChildAt(0).toString().split(" ")[1]);
            System.out.println(". Expected either another <Int_Expresison> or <Id>");
        }
        // end of integer expression checks

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
    
    private static void skipCSTNodes(int num) {
        // used to skip num nodes in the CST
        for (int i = 0; i < num; i++) {
            currentCSTNode = (DefaultMutableTreeNode) cstEnumeration.nextElement();
        }
    }
    
    private static void addASTNode() {
        //adds a node to the AST
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(currentCSTNode.toString());
        currentASTNode.add(newNode);
        currentASTNode = newNode;
        System.out.println("Semantic Analysis: Added to AST: " + newNode.toString().split(" ")[0]);
    }
   
    /* 
        The following code is for the Symbol Table 
    */

    private static void printSymbolTable(int x) {
        System.out.println("Semantic Analysis: Printing Symbol Table Tree...");

        Enumeration<TreeNode> symbolTableE = symbolTable.preorderEnumeration();

        while (symbolTableE.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) symbolTableE.nextElement();
            for (int i = 0; i < node.getLevel(); i++) {
                System.out.print("-");
            }
            System.out.println(node.getUserObject().toString());
        }
        System.out.println();
    }

    private static void printSymbolTable() {
        System.out.println("Semantic Analysis: Printing Symbol Table...");

        System.out.println("--------------------------------------");
        System.out.println("Name\tType\tScope\tisInit\tisUsed");
        System.out.println("--------------------------------------");

        Enumeration<TreeNode> symbolTableE = symbolTable.preorderEnumeration();
        ArrayList<Integer> scopeTracker = new ArrayList<Integer>();

        while (symbolTableE.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) symbolTableE.nextElement();
            Hashtable<String, VariableInfo> scope = (Hashtable<String, VariableInfo>) node.getUserObject();
            Enumeration<String> scopeE = scope.keys();

            if (node.getLevel() > scopeTracker.size() - 1) {
                scopeTracker.add(0);
            }
            else {
                scopeTracker.set(node.getLevel(), scopeTracker.get(node.getLevel() - 1) + 1);
            }

            while (scopeE.hasMoreElements()) {
                String key = scopeE.nextElement();
                VariableInfo info = (VariableInfo) scope.get(key);
                System.out.println(key + "\t" + info.getType() + "\t" + node.getLevel() + "," + scopeTracker.get(node.getLevel()) + "\t" + info.isInitialized() + "\t" + info.isUsed());
            }
        }
    }

    private static void generateSymbolTable() {
        while(astEnumeration.hasMoreElements()) {
            if (hasError) {
                return;
            }
            nextASTNode();

            // makes sure that current scope can backtrack when a block is exited
            boolean correctScope = false;
            while (!correctScope) {
                if (currentASTNode.getLevel() <= scopeBacktrackDepths.get(scopeBacktrackDepths.size() - 1)) {
                    currentSymbolTableNode = (DefaultMutableTreeNode) currentSymbolTableNode.getParent();
                    scopeBacktrackDepths.remove(scopeBacktrackDepths.size() - 1);
                }
                else {
                    correctScope = true;
                }
            }

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
            }
        }
    }
    // create a new scope when a block is found
    private static void addSymbolTableNode() {
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new Hashtable<String , VariableInfo>());
        currentSymbolTableNode.add(newNode);
        currentSymbolTableNode = newNode;
        scopeBacktrackDepths.add(currentASTNode.getLevel());
        System.out.println("Semantic Analysis: Added new scope to the symbol table");
    }

    private static void checkPrintStatement() {
        boolean isValid;

        nextASTNode();
        if (Character.isLetter(currentASTNode.getUserObject().toString().charAt(0)) && currentASTNode.getUserObject().toString().split(" ")[0].length() == 1) {

            isValid = checkVariableScope(currentASTNode.toString().split(" ")[0]);
            if (!isValid) {
                System.out.println("Semantic Analysis: Error: Variable " + currentASTNode.toString().split(" ")[0] +
                     " is not in scope near line " + currentASTNode.getUserObject().toString().split(" ")[1]);
                hasError = true;
            }
            else {
                isValid = checkVariableInitialization(currentASTNode.toString().split(" ")[0]);
                if (!isValid) {
                    System.out.println("Semantic Analysis: Error: Variable " + currentASTNode.toString().split(" ")[0] +
                     " is not initialized near line " + currentASTNode.getUserObject().toString().split(" ")[1]);
                    hasError = true;
                }
            }
        }
        else {
            // nothing to check
        }
    }

    private static void checkAssignmentStatement() {
        boolean isValid;
        String variable;

        nextASTNode();
        variable = currentASTNode.toString().split(" ")[0];
        isValid = checkVariableScope(variable);
        if (!isValid) {
            System.out.println("Semantic Analysis: Error: Variable " + currentASTNode.toString().split(" ")[0] +
                 " is not in scope near line " + currentASTNode.getUserObject().toString().split(" ")[1]);
            hasError = true;
        }
        else {
            markAsInitialized(variable);
            isValid = checkVariableType(variable);
            if (!isValid) {
                hasError = true;
            }
        }
    }

    private static void VariableDecleration() {
        boolean isValid;
        nextASTNode();
        String type = currentASTNode.toString().split(" ")[0];
        nextASTNode();
        String var = currentASTNode.toString().split(" ")[0];

        isValid = checkCurrentScope();
        if (!isValid) {
            System.out.println("Semantic Analysis: Error: Variable " + var + " has already been declared in this scope near line " +
                currentASTNode.getUserObject().toString().split(" ")[1]);
            hasError = true;
        }
        else {
            addToHashTable(type , var);
        }
    }
    // while and if are identical
    private static void checkWhileStatement() {
        checkIfStatement();
    }
    // while and if are identical
    private static void checkIfStatement() {
        if (checkBooleanExpression()) {
            // nothing to check
            nextASTNode();
        }
        else {
            boolean isValid;
            ArrayList<String> operands = new ArrayList<String>();
            nextASTNode();
            int line = -1;
            if (currentASTNode.toString().split(" ").length > 1) {
                line = Integer.parseInt(currentASTNode.getUserObject().toString().split(" ")[1]);
            } 

            do {
                operands.add(currentASTNode.toString().split(" ")[0]);
                nextASTNode();
            } while (currentASTNode.toString().split(" ")[0] != "<Block>");

            for (int i = 0; i < operands.size(); i++) {
                if (Character.isLetter(operands.get(i).charAt(0)) && operands.get(i).length() == 1) {

                    isValid = checkVariableScope(operands.get(i));
                    if (!isValid) {
                        System.out.println("Semantic Analysis: Error: Variable " + operands.get(i) +
                         " is not in scope near line " + line);
                        hasError = true;
                        return;
                    }
                    else {
                        isValid = checkVariableInitialization(operands.get(i));
                        if (!isValid) {
                            System.out.println("Semantic Analysis: Error: Variable " + operands.get(i) +
                             " is not initialized near line " + line);
                            hasError = true;
                            return;
                        }
                    }

                    // replace variable with its type in the list
                    operands.set(i, getVariableType(getScope(operands.get(i)), operands.get(i)));
                }
                else {
                    // replace the operand with its type in the list
                    operands.set(i, getDataType(operands.get(i)));
                }
            }

            // compare operand types
            for (int i = 0; i < operands.size(); i += 2) {
                if (!(operands.get(i).equals(operands.get(i + 1)))) {
                    System.out.println("Semantic Analysis: Error: Cannot compare type: " + operands.get(i) + " with type: " + operands.get(i + 1) +
                         " found near line " + line);
                    hasError = true;
                    return;
                }
            }
        }
        addSymbolTableNode();
    }

    private static boolean checkVariableScope(String variable) {
        boolean found = false;
        boolean moreToSearch = true;
        DefaultMutableTreeNode currentScope = currentSymbolTableNode;
        Hashtable<String , VariableInfo> hashTable;

        while (moreToSearch && !found) {
            hashTable = (Hashtable<String , VariableInfo>) currentScope.getUserObject();

            if (hashTable.containsKey(variable)) {
                found = true;
            }

            if (currentScope.getParent() != null) {
                currentScope = (DefaultMutableTreeNode) currentScope.getParent();
            }
            else {
                moreToSearch = false;
            }
        }
        return found;
    }

    private static boolean checkCurrentScope() {
        boolean found;
        found = !((Hashtable<String , VariableInfo>) currentSymbolTableNode.getUserObject()).containsKey(currentASTNode.toString().split(" ")[0]);
        return found;
    }

    private static boolean checkVariableType(String variable) {
        boolean typesMatch = false;
        DefaultMutableTreeNode scope = getScope(variable);
        String variableType = getVariableType(scope , variable);
        nextASTNode();
        String dataType = getDataType(currentASTNode.toString().split(" ")[0]);


        if (variableType.equals(dataType)) {
            typesMatch = true;
        }
        else {
            System.out.println("Semantic Analysis: Error: Variable " + variable + " is of type " + variableType + " and cannot be assigned a value of type " +
                dataType + "found near line " + currentASTNode.getUserObject().toString().split(" ")[1]);
            hasError = true;
        }

        return typesMatch;
    }

    private static boolean checkVariableInitialization(String variable) {
        boolean initialized = false;
        DefaultMutableTreeNode scope = getScope(variable);
        Hashtable<String , VariableInfo> hashTable = (Hashtable<String , VariableInfo>) scope.getUserObject();

        if (hashTable.get(variable).isInitialized()) {
            hashTable.get(variable).setUsed(true);
            System.out.println("Semantic Analysis: Variable " + variable + " has been used");
            initialized = true;
        }
        return initialized;
    }

    private static String getVariableType(DefaultMutableTreeNode scope , String variable) {
        String type = "";
        Hashtable<String , VariableInfo> hashTable = (Hashtable<String , VariableInfo>) scope.getUserObject();
        type = hashTable.get(variable).getType();
        return type;
    }

    private static String getDataType(String data) {
        String type;
        switch (data.charAt(0)) {
            case '<':
                if (data.charAt(1) == 'S') {
                    type = "int";
                }
                else {
                    type = "boolean";
                }
                break;
            case 't':
                type = "boolean";
                break;
            case 'f':
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

    private static DefaultMutableTreeNode getScope(String variable) {
        boolean moreToSearch = true;
        DefaultMutableTreeNode scope = null;
        DefaultMutableTreeNode currentScope = currentSymbolTableNode;
        Hashtable<String , VariableInfo> hashTable;

        while (moreToSearch) {
            hashTable = (Hashtable<String , VariableInfo>) currentScope.getUserObject();
            if (hashTable.containsKey(variable)) {
                scope = currentScope;
                moreToSearch = false;
            }
            if (currentScope.getParent() != null) {
                currentScope = (DefaultMutableTreeNode) currentScope.getParent();
            }
            else {
                moreToSearch = false;
            }
        }

        if (scope == null) {
            // probably not needed
            System.out.println("Semantic Analysis: Error: Variable " + variable + " is not in scope near line " +
                 currentASTNode.getUserObject().toString().split(" ")[1]);
            hasError = true;
        }

        return scope;
    }
    // returns true for boolval and false for a comparison
    private static boolean checkBooleanExpression() {
        nextASTNode();
        if (currentASTNode.toString().split(" ")[0].equals("true") || currentASTNode.toString().split(" ")[0].equals("false")) {
            return true;
        }
        else {
            return false;
        }
    }

    private static void nextASTNode() {
        currentASTNode = (DefaultMutableTreeNode) astEnumeration.nextElement();
    }

    private static void addToHashTable(String type , String var) {
        Hashtable<String , VariableInfo> hashTable = (Hashtable<String , VariableInfo>) currentSymbolTableNode.getUserObject();
        hashTable.put(var , new VariableInfo(type));
        System.out.println("Semantic Analysis: added Variable " + var + " of type " + type + " added to the symbol table");
    }

    private static void markAsInitialized(String variable) {
        DefaultMutableTreeNode scope = getScope(variable);
        Hashtable<String , VariableInfo> hashTable = (Hashtable<String , VariableInfo>) scope.getUserObject();
        hashTable.get(variable).setInitialized(true);
        System.out.println("Semantic Analysis: Variable " + variable + " has been initialized");
    }

    private static void checkVariablesUsed() {
        System.out.println(); // for formatting
        Enumeration<TreeNode> scopeEnumeration = symbolTable.preorderEnumeration();
        ArrayList<Integer> scopeTracker = new ArrayList<Integer>();

        while (scopeEnumeration.hasMoreElements()) {
            DefaultMutableTreeNode scope = (DefaultMutableTreeNode) scopeEnumeration.nextElement();
            Hashtable<String , VariableInfo> hashTable = (Hashtable<String , VariableInfo>) scope.getUserObject();
            Enumeration<String> variableEnumeration = hashTable.keys();

            if (scope.getLevel() > scopeTracker.size() - 1) {
                scopeTracker.add(0);
            }
            else {
                scopeTracker.set(scope.getLevel(), scopeTracker.get(scope.getLevel() - 1) + 1);
            }

            while (variableEnumeration.hasMoreElements()) {
                String variable = variableEnumeration.nextElement();
                if (!hashTable.get(variable).isInitialized()) {
                    System.out.println("Semantic Analysis: Warning: Variable " + variable + " of type " + hashTable.get(variable).getType() +
                         " in scope " + scope.getLevel() + "," + scopeTracker.get(scope.getLevel()) + " is never initialized");
                }
                else if (!hashTable.get(variable).isUsed()) {
                    System.out.println("Semantic Analysis: Warning: Variable " + variable + " of type " + hashTable.get(variable).getType() +
                         " in scope " + scope.getLevel() + "," + scopeTracker.get(scope.getLevel()) + " is never used");
                }
            }
        }
    }
}
