import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

// used to copy code to clipboard
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

public class CodeGeneration {

    private static boolean verbose = true;
    private static boolean hasError;
    private static ArrayList<String> code;
    private static ArrayList<StaticTableData> staticTable;
    private static ArrayList<Integer> scopeBacktrackDepths;
    private static ArrayList<Integer> jumps;
    private static ArrayList<String> messages;
    private static ArrayList<Integer> whileLoopStart;
    private static ArrayList<Integer> whileLoopUntil;
    private static DefaultMutableTreeNode ast;
    private static DefaultMutableTreeNode symbolTable;
    private static DefaultMutableTreeNode currentASTNode;
    private static DefaultMutableTreeNode currentScope;
    private static Enumeration<TreeNode> scopEnumeration;
    private static Enumeration<TreeNode> astEnumeration;
    private static Hashtable<DefaultMutableTreeNode , Integer> scopeNames;
    private static Hashtable<String , String> stringsInHeap;
    private static Hashtable<String , Integer> jumpTable;
    private static int currentMemoryLocation;
    private static int heapDepth;
    private static int skipCodeGenUntil;
    private static String[] booleansInMemory = new String[2];
    private static boolean codeGenPaused;

    public static String doCodeGeneration(DefaultMutableTreeNode[] roots) {
        if (roots == null) {
            return "Skipped Code Generation due to compilation error";
        }

        initializeVariables(roots);
        generateCode();
        giveVariablesAddresses();
        setJumpDistances();

        return codeArrayToString();
    }

    private static void initializeVariables(DefaultMutableTreeNode[] roots) {
        hasError = false;
        initalizeCode();
        staticTable = new ArrayList<>();
        scopeBacktrackDepths = new ArrayList<>();
        jumps = new ArrayList<>();
        messages = new ArrayList<>();
        whileLoopStart = new ArrayList<>();
        whileLoopUntil = new ArrayList<>();
        ast = roots[0];
        symbolTable = roots[1];
        currentASTNode = ast;
        currentScope = symbolTable;
        scopEnumeration = symbolTable.preorderEnumeration();
        astEnumeration = ast.preorderEnumeration();
        initializeScopeNames();
        stringsInHeap = new Hashtable<>();
        jumpTable = new Hashtable<>();
        currentMemoryLocation = 0x00;
        heapDepth = 0xFF;
        skipCodeGenUntil = Integer.MAX_VALUE;
        booleansInMemory[0] = "00"; booleansInMemory[1] = "00";
        codeGenPaused = false;
    }

    private static void initalizeCode() {
        // initialize code array with 256 bytes of 00
        code = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            code.add("00");
        }
    }

    private static void initializeScopeNames() {
        // initialize scope names
        scopeNames = new Hashtable<>();
        Enumeration<TreeNode> enumeration = symbolTable.preorderEnumeration();
        while(enumeration.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
            scopeNames.put(node, scopeNames.size());
        }
    }

    private static void generateCode() {
        while(astEnumeration.hasMoreElements()) {
            // check for out of memory error
            if (currentMemoryLocation > heapDepth || code.size() > 256) {
                hasError = true;
                System.out.println("Code Generation Error: Exceeded 256 byte memory limit");
            } 
            if (hasError) { return; }

            nextASTNode();

            // makes sure that current scope can backtrack when a block is exited
            boolean correctScope = false;
            while (!correctScope) {
                if (scopeBacktrackDepths.size() > 0) {
                    if (currentASTNode.getLevel() <= scopeBacktrackDepths.get(scopeBacktrackDepths.size() - 1) && currentASTNode.getLevel() != 0) {
                        exitBlock();
                    }
                    else {
                        correctScope = true;
                    }
                }
                else {
                    correctScope = true;
                }
            }
            switch(currentASTNode.toString()){
                case "<Block>":
                    enterBlock();
                    break;
                case "<Print_Statement>":
                    Print_Statement();
                    break;
                case "<Assignment_Statement>":
                    Assignment_Statement();
                    break;
                case "<Variable_Decleration>":
                    Variable_Decleration();
                    break;
                case "<While_Statement>":
                    While_Statement();
                    break;
                case "<If_Statement>":
                    If_Statement();
                    break;
            }
        }
        while (currentScope.getParent() != null) {
            exitBlock();
        }
        // final break
        addCodeToMemory("00");
    }

    private static void addToStaticDataTable(String var) {
        staticTable.add(new StaticTableData("T" + staticTable.size() , var , scopeNames.get(currentScope) , staticTable.size()));
    }

    private static void enterBlock() {
        // go to proper scope
        currentScope = (DefaultMutableTreeNode) scopEnumeration.nextElement();
        scopeBacktrackDepths.add(currentASTNode.getLevel());
    }

    private static void exitBlock() {
        if (whileLoopUntil.size() > 0) {
            if (whileLoopUntil.get(whileLoopUntil.size() - 1) < scopeBacktrackDepths.size()) {
                // do nothing, still in while block
            }
            else {
                // unconditional branch to the top of the while loop
                // load x reg with 1
                addCodeToMemory("A2");
                addCodeToMemory("01");
    
                // compare x reg to 0xFF
                addCodeToMemory("EC");
                addCodeToMemory("FF");
                addCodeToMemory("00");
    
                // branch n bytes to the start of the while loop
                jumpTable.put("J" + jumpTable.size() , 0xFF - currentMemoryLocation + (whileLoopStart.get(whileLoopStart.size() - 1) - 1));
                addCodeToMemory("D0");
                addCodeToMemory("J" + (jumpTable.size()-1));
            }
        }

        // go to parent scope
        currentScope = (DefaultMutableTreeNode) currentScope.getParent();
        scopeBacktrackDepths.remove(scopeBacktrackDepths.size() - 1);
        if (jumps.size() > 0) {
            jumps.set(jumps.size() - 1, currentMemoryLocation - jumps.get(jumps.size() - 1) - 1);
            for (int i = jumpTable.size(); i > 0; i--) {
                if (jumpTable.get("J" + (i - 1)) == -1) {
                    jumpTable.put("J" + (i - 1), jumps.get(jumps.size() - 1));
                    jumps.remove(jumps.size() - 1);
                    break;
                }
            }
        }
    }

    private static void Print_Statement() { 
        nextASTNode();
        if (currentASTNode.toString().split(" ")[0].length() == 1) {
            // print single digit
            if (Character.isDigit(currentASTNode.toString().charAt(0))) {
                // load x reg with 01
                addCodeToMemory("A2");
                addCodeToMemory("01");

                // load y reg with digit
                addCodeToMemory("A0");
                addCodeToMemory("0" + currentASTNode.toString().split(" ")[0]);
            }
            // print variable
            else {
                // get the location of the variable
                String location = getVarLocation(currentASTNode.toString().split(" ")[0], currentScope);
                // get the type of the variable
                String varType = getVarType(currentScope ,  currentASTNode.toString().split(" ")[0]);

                // if the variable is a string
                if(varType.equals("string")) {
                    // load x reg with 02
                    addCodeToMemory("A2");
                    addCodeToMemory("02");

                    // load y reg with pointer to string
                    addCodeToMemory("AC");
                    addCodeToMemory(location);
                    addCodeToMemory("00");
                }
                // if the variable is a boolean
                else if(varType.equals("boolean")) {

                    //
                    // FIX THIS LATER (booleans are printed as 1 or 0)
                    // if this doesnt change then change printing boolean literals
                    //

                    // load x reg with 01
                    addCodeToMemory("A2");
                    addCodeToMemory("01");

                    // load y reg with data at location
                    addCodeToMemory("AC");
                    addCodeToMemory(location);
                    addCodeToMemory("00");
                }
                // if the variable is an int
                else {
                    // load x reg with 01
                    addCodeToMemory("A2");
                    addCodeToMemory("01");

                    // load y reg with data at location
                    addCodeToMemory("AC");
                    addCodeToMemory(location);
                    addCodeToMemory("00");
                }
            }
        }
        // print string literal
        else if (currentASTNode.toString().charAt(0) == '"') {
            String strAddress = "";
            // add string to heap if it is not already there
            if(!(stringsInHeap.containsKey(currentASTNode.toString().replaceAll("\"", "")))) {
                addStringToHeap(currentASTNode.toString());
                strAddress = intToHexString(heapDepth + 1);
            }
            else {
                // if the string is already in the heap then get the pointer to it
                strAddress = stringsInHeap.get(currentASTNode.toString().replaceAll("\"", ""));
            }
            // load x reg with 02
            addCodeToMemory("A2");
            addCodeToMemory("02");

            // load y reg with pointer to string
            addCodeToMemory("A0");
            addCodeToMemory(strAddress);
        }
        // print a sum
        else if(currentASTNode.toString().charAt(0) == '<') {

            if (currentASTNode.toString().equals("<Sum_Of>")) {

                // load x reg with 01
                addCodeToMemory("A2");
                addCodeToMemory("01");

                // add sum code
                Sum_Of(true);

                // store sum in memory
                addCodeToMemory("8D");
                addCodeToMemory("00");
                addCodeToMemory("00");

                // load y reg with sum
                addCodeToMemory("AC");
                addCodeToMemory("00");
                addCodeToMemory("00");
            }
            // print boolean expression
            else {
                Boolean_Expression(true);
            }
        }
        // print a boolean
        else {
            // load x reg with 01
            addCodeToMemory("A2");
            addCodeToMemory("01");
            // print true
            if(currentASTNode.toString().split(" ")[0].equals("true")) {
                // load y reg with 01
                addCodeToMemory("A0");
                addCodeToMemory("01");
            }
            // print false
            else {
                // load y reg with 00
                addCodeToMemory("A0");
                addCodeToMemory("00");
            }
        }
        // system call
        addCodeToMemory("FF");
    }

    private static void Assignment_Statement() {
        // get the temp address for the variable
        nextASTNode();
        String tempLocation = getVarLocation(currentASTNode.toString().split(" ")[0], currentScope);

        nextASTNode();
        // assign a sum
        if(currentASTNode.toString().charAt(0) == '<') {
            // add sum code
            Sum_Of(true);

            // store sum in the variable location
            addCodeToMemory("8D");
            addCodeToMemory(tempLocation);
            addCodeToMemory("00");
        }
        // assign a string
        else if(currentASTNode.toString().charAt(0) == '"') {
            String strAddress = "";
            // add string to heap if it is not already there
            if(!(stringsInHeap.containsKey(currentASTNode.toString().replaceAll("\"", "")))) {
                addStringToHeap(currentASTNode.toString());
                strAddress = intToHexString(heapDepth + 1);
            }
            else {
                // if the string is already in the heap then get the pointer to it
                strAddress = stringsInHeap.get(currentASTNode.toString().replaceAll("\"", ""));
            }

            // load acc with pointer to string
            addCodeToMemory("A9");
            addCodeToMemory(strAddress);

            // store pointer to string in variable location
            addCodeToMemory("8D");
            addCodeToMemory(tempLocation);
            addCodeToMemory("00");
        }
        // assign a digit
        else if(Character.isDigit(currentASTNode.toString().charAt(0))) {
            // load acc with digit
            addCodeToMemory("A9");
            addCodeToMemory("0" + currentASTNode.toString().split(" ")[0]);

            // store acc in variable location
            addCodeToMemory("8D");
            addCodeToMemory(tempLocation);
            addCodeToMemory("00");
        }
        // assign a variable
        else if(currentASTNode.toString().split(" ")[0].length() == 1) {
            // get the temp address for the assignee variable
            String tempLocationB = getVarLocation(currentASTNode.toString().split(" ")[0], currentScope);

            // load acc with value of assignee variable
            addCodeToMemory("AD");
            addCodeToMemory(tempLocationB);
            addCodeToMemory("00");

            // store acc in variable location
            addCodeToMemory("8D");
            addCodeToMemory(tempLocation);
            addCodeToMemory("00");
        }
        // assign a boolean
        else {
            if(currentASTNode.toString().split(" ")[0].equals("true")) {
                // load acc with 01
                addCodeToMemory("A9");
                addCodeToMemory("01");

            }
            else {
                // load acc with 00
                addCodeToMemory("A9");
                addCodeToMemory("00");
            }
            // store acc in variable location
            addCodeToMemory("8D");
            addCodeToMemory(tempLocation);
            addCodeToMemory("00");
        }
    }

    private static void Variable_Decleration() {
        // add variable to static table
        nextASTNode();
        String varType = currentASTNode.toString().split(" ")[0];
        nextASTNode();

        addToStaticDataTable(currentASTNode.toString().split(" ")[0]);

        // check type, if string do nothing, otherwise initialize to 0/false
        if(varType.equals("string")) {
            // do nothing
        }
        else {
            // load acc with 0: A9 00
            addCodeToMemory("A9");
            addCodeToMemory("00");

            // store acc in mem temp: 8D T? XX
            addCodeToMemory("8D");
            addCodeToMemory("T" + (staticTable.size() - 1));
            addCodeToMemory("00");
        }
    }

    private static void While_Statement() {
        whileLoopStart.add(currentMemoryLocation);
        nextASTNode();
        if (currentASTNode.toString().charAt(0) == '<') {
            Boolean_Expression(false);
        }
        else {
            if (currentASTNode.toString().split(" ")[0].equals("false")) {
                // skip code gen until the end of the block
                skipBlock(true);
            }
            else {
                // condition true, generate a warning for infinie loop
                messages.add("Warning: Infinite loop detected at line " + currentASTNode.toString().split(" ")[1] + ".");
            }
        }
        // add the depth of the current scope to the backtrack stack
        whileLoopUntil.add(scopeBacktrackDepths.size() + 1);
    }

    private static void If_Statement() {
        nextASTNode();
        if (currentASTNode.toString().charAt(0) == '<') {
            Boolean_Expression(false);
        }
        else {
            if (currentASTNode.toString().split(" ")[0].equals("false")) {
                // if the comparison returns false
                // branch to the end of the block
                // or we could skip code gen until the end of the block
                skipBlock(true);
            }
            else {
                // condition true, continue with code generation, generate a message for unneccesary conditional
                messages.add("Info: Always true conditional found");
            }
        }
    }

    private static void Sum_Of(boolean first) {
        // will leave result in accumulator

        // if it is the first operand
        if(first) {
            // load acc with the first operand
            nextASTNode();
            addCodeToMemory("A9");
            addCodeToMemory("0" + currentASTNode.toString().split(" ")[0]);
        }
        // if there is already part of the sum in the accumulator
        else {
            // store the accumulator in memory
            String digitLocation = "00";
            addCodeToMemory("8D");
            addCodeToMemory(digitLocation);
            addCodeToMemory("00");

            // load accumulator with the next digit
            nextASTNode();
            addCodeToMemory("A9");
            addCodeToMemory("0" + currentASTNode.toString().split(" ")[0]);

            // ADC with the digit in memory
            addCodeToMemory("6D");
            addCodeToMemory(digitLocation);
            addCodeToMemory("00");
        }

        // check next operand
        nextASTNode();
        if(currentASTNode.toString().split(" ")[0].length() == 1) {
            // if a digit is the next operand
            if(Character.isDigit(currentASTNode.toString().split(" ")[0].charAt(0))) {
                // store the accumulator in memory
                String digitLocation = "00";
                addCodeToMemory("8D");
                addCodeToMemory(digitLocation);
                addCodeToMemory("00");

                // load accumulator with the next digit
                addCodeToMemory("A9");
                addCodeToMemory("0" + currentASTNode.toString().split(" ")[0]);

                // ADC with the digit in memory
                addCodeToMemory("6D");
                addCodeToMemory(digitLocation);
                addCodeToMemory("00");
            }
            // if a variable is the next operand
            else {
                // ADC with the variable in memory
                addCodeToMemory("6D");
                addCodeToMemory(getVarLocation(currentASTNode.toString().split(" ")[0] , currentScope)); 
                addCodeToMemory("00");
            }
        }
        // if the next operand is another Sum_Of
        else {
            // do recursion
            Sum_Of(false);
        }
    }

    private static void Boolean_Expression(boolean inPrint) {
        // store comparison type in isEquivalence
        boolean isEquivalence = false;
        if (currentASTNode.toString().split(" ")[0].equals("<Is_Equal>")) {
            isEquivalence = true;
        }
        nextASTNode();

        // if the first operand is a string literal
        if (currentASTNode.toString().charAt(0) == '"') {

            // check stringsInHeap for the string
            String tempString = "";
            if (stringsInHeap.containsKey(currentASTNode.toString().split(" ")[0].replaceAll("\"", ""))) {

                // store the address of the string in strLocation and the string in tempString
                String strLocation = stringsInHeap.get(currentASTNode.toString().split(" ")[0].replaceAll("\"", ""));
                tempString = currentASTNode.toString().split(" ")[0];

                nextASTNode();
                // check the next operand
                // if the next operand is a string literal
                if (currentASTNode.toString().charAt(0) == '"') {
                    if (!(tempString.equals(currentASTNode.toString().split(" ")[0]) ^ isEquivalence)) { 
                        if (inPrint) {
                            // load y reg with 1
                            addCodeToMemory("A0");
                            addCodeToMemory("01");
                            return;
                        }
                        // comparison is true so we can ignore branching code
                        newMessage();
                    }
                    else {
                        if (inPrint) {
                            // load y reg with 0
                            addCodeToMemory("A0");
                            addCodeToMemory("00");
                            return;
                        }
                        // if the comparison returns false
                        // branch to the end of the block
                        // or we could skip code gen until the end of the block
                        skipBlock(true);
                    }
                    
                }
                // if the next operand is a variable
                else {

                    // add str location to x reg
                    addCodeToMemory("A2");
                    addCodeToMemory(strLocation);

                    if (inPrint) {
                        printComaprison(getVarLocation(currentASTNode.toString().split(" ")[0] , currentScope) , isEquivalence);
                        return;
                    }

                    // does the comparison for == or !=
                    doComparison(currentASTNode.toString().split(" ")[0] , isEquivalence);
                }
            }
            // if the string is not in the heap
            else {
                tempString = currentASTNode.toString().split(" ")[0];
                nextASTNode();
                // check the next operand
                // if the next operand is a string literal
                if (currentASTNode.toString().charAt(0) == '"') {
                    if (!(tempString.equals(currentASTNode.toString().split(" ")[0]) ^ isEquivalence)) {
                        if (inPrint) {
                            // load y reg with 1
                            addCodeToMemory("A0");
                            addCodeToMemory("01");
                            return;
                        }
                        // comparison is true so we can ignore branching code
                        newMessage();
                    }
                    else {
                        if (inPrint) {
                            // load y reg with 0
                            addCodeToMemory("A0");
                            addCodeToMemory("00");
                            return;
                        }
                        // if the comparison returns false
                        // branch to the end of the block
                        // or we could skip code gen until the end of the block
                        skipBlock(true);
                    }
                }
                // if the next operand is a variable
                else {
                    if (inPrint) {
                        // load y reg with 0
                        addCodeToMemory("A0");
                        addCodeToMemory("00");
                        return;
                    }
                    // comparison must be false if the string is not in the heap
                    // branch to the end of the block
                    // or we could skip code gen until the end of the block 
                    skipBlock(true);
                }
            }
        }
        // if the first operand is a boolval
        else if (currentASTNode.toString().split(" ")[0].length() > 1 && currentASTNode.toString().split(" ")[0].length() < 8) {
            boolean firstOperand = false;
            if (currentASTNode.toString().split(" ")[0].equals("true")) {
                firstOperand = true;
            }

            nextASTNode();
            // check the next operand
            // if the next operand is a boolval
            if (currentASTNode.toString().split(" ")[0].length() > 1) {
                boolean secondOperand = false;
                if (currentASTNode.toString().split(" ")[0].equals("true")) {
                    secondOperand = true;
                }
                if (!(firstOperand == secondOperand) ^ isEquivalence) {
                    if (inPrint) {
                        // load y reg with 1
                        addCodeToMemory("A0");
                        addCodeToMemory("01");
                        return;
                    }
                    // comparison is true so we can ignore branching code
                    newMessage();
                }
                else {
                    if (inPrint) {
                        // load y reg with 0
                        addCodeToMemory("A0");
                        addCodeToMemory("00");
                        return;
                    }
                    // if the comparison returns false
                    // branch to the end of the block
                    // or we could skip code gen until the end of the block
                    skipBlock(true);
                }
            }
            // if the next operand is a variable
            else {
                if (firstOperand) {
                    // load x reg with 1
                    addCodeToMemory("A2");
                    addCodeToMemory("01");
                }
                else {
                    // load x reg with 0
                    addCodeToMemory("A2");
                    addCodeToMemory("00");
                }
                if (inPrint) {
                    printComaprison(getVarLocation(currentASTNode.toString().split(" ")[0] , currentScope) , isEquivalence);
                    return;
                }
                // does the comparison for == or !=
                doComparison(currentASTNode.toString().split(" ")[0] , isEquivalence);
            }
        }

        // if the first operand is a digit
        else if (Character.isDigit(currentASTNode.toString().split(" ")[0].charAt(0))) {
            int firstOperand = Integer.parseInt(currentASTNode.toString().split(" ")[0]);
            nextASTNode();
            // check the next operand
            // if the next operand is a digit
            if (Character.isDigit(currentASTNode.toString().split(" ")[0].charAt(0))) {
                int secondOperand = Integer.parseInt(currentASTNode.toString().split(" ")[0]);
                if (!(firstOperand == secondOperand) ^ isEquivalence) {
                    if (inPrint) {
                        // load y reg with 1
                        addCodeToMemory("A0");
                        addCodeToMemory("01");
                        return;
                    }
                    // comparison is true so we can ignore branching code
                    newMessage();
                }
                else {
                    if (inPrint) {
                        // load y reg with 0
                        addCodeToMemory("A0");
                        addCodeToMemory("00");
                        return;
                    }
                    // if the comparison returns false
                    // branch to the end of the block
                    // or we could skip code gen until the end of the block
                    skipBlock(true);
                }
            }
            // if the next operand is a variable
            else if (currentASTNode.toString().split(" ")[0].length() == 1) {
                // load x reg with first operand
                addCodeToMemory("A2");
                addCodeToMemory(intToHexString(firstOperand));

                if (inPrint) {
                    // load y reg with 0
                    addCodeToMemory("A0");
                    addCodeToMemory("00");
                    return;
                }
                // does the comparison for == or !=
                doComparison(currentASTNode.toString().split(" ")[0] , isEquivalence);
            }
            // if the next operand is a sum
            else {
                int secondOperand = 0;
                while (!currentASTNode.toString().equals("<Block>")) {
                    nextASTNode();
                    if (Character.isDigit(currentASTNode.toString().split(" ")[0].charAt(0))) {
                        secondOperand += Integer.parseInt(currentASTNode.toString().split(" ")[0]);
                    }
                }
                if (firstOperand == secondOperand) {
                    if (inPrint) {
                        // load y reg with 1
                        addCodeToMemory("A0");
                        addCodeToMemory("01");
                        return;
                    }
                    // comparison is true so we can ignore branching code
                    newMessage();
                }
                else {
                    if (inPrint) {
                        // load y reg with 0
                        addCodeToMemory("A0");
                        addCodeToMemory("00");
                        return;
                    }
                    // if the comparison returns false
                    // branch to the end of the block
                    // or we could skip code gen until the end of the block
                    skipBlock(true);
                }
            }
        }
        // if the first operand is a variable
        else if (currentASTNode.toString().split(" ")[0].length() == 1) {
            String varLocation = getVarLocation(currentASTNode.toString().split(" ")[0] , currentScope);
            String varType = getVarType(currentScope , currentASTNode.toString().split(" ")[0]);
            nextASTNode();

            // if the next operand is a var
            if (!(Character.isDigit(currentASTNode.toString().split(" ")[0].charAt(0))) && currentASTNode.toString().split(" ")[0].length() == 1) {
                
                // load x reg with varLocation
                addCodeToMemory("AE");
                addCodeToMemory(varLocation);
                addCodeToMemory("00");

                varLocation = getVarLocation(currentASTNode.toString().split(" ")[0] , currentScope);

                if (inPrint) {
                    printComaprison(varLocation , isEquivalence);
                    return;
                }

                // does the comparison for == or !=
                doComparison(varLocation , isEquivalence);
            }
            else {
                switch(varType) {
                    case "int":
                        if (Character.isDigit(currentASTNode.toString().split(" ")[0].charAt(0))) {
                            // load digit into x reg
                            addCodeToMemory("A2");
                            addCodeToMemory("0" + currentASTNode.toString().split(" ")[0]);

                            if (inPrint) {
                                printComaprison(varLocation , isEquivalence);
                                return;
                            }
        
                            // does the comparison for == or !=
                            doComparison(varLocation , isEquivalence);
                        }
                        else {
                            // load sum result into acc
                            Sum_Of(true);

                            // store acc at 00
                            addCodeToMemory("8D");
                            addCodeToMemory("00");
                            addCodeToMemory("00");

                            // load x reg with varLocation
                            addCodeToMemory("AE");
                            addCodeToMemory(varLocation);
                            addCodeToMemory("00");

                            if (inPrint) {
                                printComaprison("00" , isEquivalence);
                                return;
                            }

                            // does the comparison for == or !=
                            doComparison("00" , isEquivalence);
                        }

                        break;
                    case "boolean":
                        if (currentASTNode.toString().split(" ")[0].equals("true")) {
                            // load x reg with 1
                            addCodeToMemory("A2");
                            addCodeToMemory("01");
                        }
                        else {
                            // load x reg with 0
                            addCodeToMemory("A2");
                            addCodeToMemory("00");
                        }

                        if (inPrint) {
                            printComaprison(varLocation , isEquivalence);
                            return;
                        }

                        // does the comparison for == or !=
                        doComparison(varLocation , isEquivalence);

                        break;
                    case "string":
                        // check if the string is in the heap
                        if (stringsInHeap.containsKey(currentASTNode.toString().split(" ")[0].replaceAll("\"" , ""))) {
                            String heapLocation = stringsInHeap.get(currentASTNode.toString().split(" ")[0].replaceAll("\"" , ""));

                            // load x reg with the heap location
                            addCodeToMemory("A2");
                            addCodeToMemory(heapLocation);

                            if (inPrint) {
                                printComaprison(varLocation , isEquivalence);
                                return;
                            }

                            // does the comparison for == or !=
                            doComparison(varLocation , isEquivalence);
                        }
                        else {
                            if (inPrint) {
                                // load y reg with 0
                                addCodeToMemory("A0");
                                addCodeToMemory("00");
                                return;
                            }
                            // cant be equal if the string is not in the heap
                            // branch to the end of the block
                            // or we could skip code gen until the end of the block
                            skipBlock(true);
                        }
                        break; 
                }
            }
        }
        // if the first operand is a sum
        else {
            // load acc with sum
            Sum_Of(true);

            // store acc at 00
            addCodeToMemory("8D");
            addCodeToMemory("00");
            addCodeToMemory("00");

            nextASTNode();

            // if the next operand is a digit
            if (Character.isDigit(currentASTNode.toString().split(" ")[0].charAt(0))) {
                // load x reg with digit
                addCodeToMemory("A2");
                addCodeToMemory("0" + currentASTNode.toString().split(" ")[0]);

                if (inPrint) {
                    printComaprison("00" , isEquivalence);
                    return;
                }

                // does the comparison for == or !=
                doComparison("00" , isEquivalence);
            }
            // if the next operand is a var
            else if (currentASTNode.toString().split(" ")[0].length() == 1) {
                String varLocation = getVarLocation(currentASTNode.toString().split(" ")[0] , currentScope);

                // load x reg with varLocation
                addCodeToMemory("AE");
                addCodeToMemory(varLocation);
                addCodeToMemory("00");

                if (inPrint) {
                    printComaprison(varLocation , isEquivalence);
                    return;
                }

                // does the comparison for == or !=
                doComparison(varLocation , isEquivalence);
            }
            // if the next operand is a sum
            else {
                //store 00 00 in x reg
                addCodeToMemory("AE");
                addCodeToMemory("00");
                addCodeToMemory("00");

                // load acc with sum
                Sum_Of(true);

                // store acc at 00
                addCodeToMemory("8D");
                addCodeToMemory("00");
                addCodeToMemory("00");

                if (inPrint) {
                    printComaprison("00" , isEquivalence);
                    return;
                }

                // does comparison for == or !=
                doComparison("00" , isEquivalence);
            }
        }
    }

    private static void printComaprison(String location , boolean isEquivalence) {
        int first = -1;
        int second = -1;

        if (isEquivalence) {
            first = 1;
            second = 0;
        }
        else {
            first = 0;
            second = 1;
        }

        // compare x reg to var location
        addCodeToMemory("EC");
        addCodeToMemory(location);
        addCodeToMemory("00");

        // load x reg with 1
        addCodeToMemory("A2");
        addCodeToMemory("01");

        // if they are not equal branch over the print 0
        jumpTable.put("J" + jumpTable.size() , 9); // branch 9 bytes to start of print 0
        addCodeToMemory("D0");
        addCodeToMemory("J" + (jumpTable.size()-1));

        // print first
        // load y reg with first
        addCodeToMemory("A0");
        addCodeToMemory("0" + first);

        // unconditional branch over print 0
        // load x reg with 1
        addCodeToMemory("A2");
        addCodeToMemory("01");

        // compare x reg to $FF
        addCodeToMemory("EC");
        addCodeToMemory("FF");
        addCodeToMemory("00");

        jumpTable.put("J" + jumpTable.size() , 2); // branch 2 bytes over print 0
        addCodeToMemory("D0");
        addCodeToMemory("J" + (jumpTable.size()-1));

        // print second
        // load y reg with second
        addCodeToMemory("A0");
        addCodeToMemory("0" + second);

        // system call in Print_Statement()
    }

    private static void doComparison(String location , boolean isEquivalence) {

        // compare x reg to var location
        addCodeToMemory("EC");
        addCodeToMemory(location);
        addCodeToMemory("00");

        if (isEquivalence) {
            // compare x reg to var location and BNE
            equalToCompare(location);
        }
        else {
            // compare x reg to var location branch to block on not equal, branch over if equal
            notEqualToCompare(location);
        }
    }

    private static void equalToCompare(String location) {
        // branch n bytes to the end of the block if they are not equal
        jumpTable.put("J" + jumpTable.size() , -1);
        addCodeToMemory("D0");
        jumps.add(currentMemoryLocation);
        addCodeToMemory("J" + (jumpTable.size()-1));
    }

    private static void notEqualToCompare(String location) {
        // branch n bytes to the block if they are not equal
        jumpTable.put("J" + jumpTable.size() , 7); // branch 7 bytes to start of block
        addCodeToMemory("D0");
        addCodeToMemory("J" + (jumpTable.size()-1));

        // if they are equal unconditional branch over the block
        // load x reg with 1
        addCodeToMemory("A2");
        addCodeToMemory("01");

        // compare x reg to FF
        addCodeToMemory("EC");
        addCodeToMemory("FF");
        addCodeToMemory("00");

        // compare x reg to var location and BNE
        equalToCompare("FF");
    }

    private static void addBooleanValueToMemory(String bool) {
        heapDepth -= bool.length();
        String[] hexArray = stringToAscii("\"" + bool + "\"");
        int heapPointer = heapDepth;

        for (String hexByte : hexArray) {
            if(!code.get(heapPointer).equals("00")) {
                hasError = true;
                return;
            }
            code.set(heapPointer, hexByte);
            heapPointer++;
        }

        if(bool.equals("true")) {
            booleansInMemory[0] = intToHexString(heapDepth);
        }
        else {
            booleansInMemory[1] = intToHexString(heapDepth);
        }

        heapDepth--;
    }

    private static void addCodeToMemory(String hexCode) {
        if(!code.get(currentMemoryLocation).equals("00")) {
            hasError = true;
            return;
        }
        if (skipBlock()) { return; }

        code.set(currentMemoryLocation, hexCode);
        if (verbose) { System.out.println("Added " + hexCode + " to memory location $" + intToHexString(currentMemoryLocation)); }
        currentMemoryLocation++;
    }

    private static void nextASTNode() {
        if (astEnumeration.hasMoreElements()) {
            currentASTNode = (DefaultMutableTreeNode) astEnumeration.nextElement();
            if (verbose) { System.out.println("Current AST Node: " + currentASTNode.toString()); }
        }
    }

    private static void addStringToHeap(String str) {

        if (skipBlock()) { return; }

        heapDepth -= str.replaceAll("\"", "").length();
        String[] hexArray = stringToAscii(str);
        int heapPointer = heapDepth;

        stringsInHeap.put(str.replaceAll("\"", "") , intToHexString(heapPointer));
        if (verbose) { System.out.println("Added string to heap: " + str + "to memory location $ " + intToHexString(heapPointer));}

        for (String hexByte : hexArray) {
            if(!code.get(heapPointer).equals("00")) {
                hasError = true;
                return;
            }
            code.set(heapPointer, hexByte);
            heapPointer++;
        }
        heapDepth--;
    }

    private static boolean skipBlock() {
        if (skipCodeGenUntil <= scopeBacktrackDepths.size()) {
            codeGenPaused = true;
            return true;
        }

        if (verbose && codeGenPaused) { System.out.println("\nResuming Code Generation"); }
        skipCodeGenUntil = Integer.MAX_VALUE;
        codeGenPaused = false;
        return false;
    }

    private static void skipBlock(boolean startSkip) {
        skipCodeGenUntil = scopeBacktrackDepths.size() + 1;
        if (verbose) { System.out.println("Skipping code gen until exiting this block\n"); }
        messages.add("Info: Always false conditional found");
    }

    private static String[] stringToAscii(String str) {
        str = str.substring(1, str.length() - 1);
        str = str.replaceAll("_", " ");

        String[] hexArray = new String[str.length() + 1];
        for (int i = 0; i < str.length(); i++) {
            int asciiValue = (int) str.charAt(i);
            String hexValue = intToHexString(asciiValue);
            hexArray[i] = hexValue;
        }
        hexArray[str.length()] = "00";
        return hexArray;
    }

    private static String intToHexString(int x) {
        String hexString = Integer.toHexString(x).toUpperCase();
            if (hexString.length() == 1) {
                hexString = "0" + hexString;
            }
        return hexString;
    }

    private static String getVarLocation(String var , DefaultMutableTreeNode scope) {
        String tempLocation = "Location not found";
        Hashtable<String , VariableInfo> hashTable = (Hashtable<String , VariableInfo>) scope.getUserObject();
        while (!hashTable.containsKey(var)) {
            scope = (DefaultMutableTreeNode) scope.getParent();
            hashTable = (Hashtable<String , VariableInfo>) scope.getUserObject();
        }

        for(int i = 0; i < staticTable.size(); i++) {
            if (staticTable.get(i).getVar().equals(var)) {
                if (staticTable.get(i).getScope() == scopeNames.get(scope)) {
                    tempLocation = staticTable.get(i).getTemp();
                    break;
                }
            }
        }
        return tempLocation;
    }

    private static String getVarType(DefaultMutableTreeNode scope , String variable) {
        String type = "";
        Hashtable<String , VariableInfo> hashTable = (Hashtable<String , VariableInfo>) scope.getUserObject();
        while (!hashTable.containsKey(variable)) {
            scope = (DefaultMutableTreeNode) scope.getParent();
            hashTable = (Hashtable<String , VariableInfo>) scope.getUserObject();
        }
        type = hashTable.get(variable).getType();
        return type;
    }

    private static String codeArrayToString() {
        if (hasError) {
            return "Code exceeded 256 byte limit";
        }
        int i = 0;
        String codeString = "";
        String testString = "";
        for (String hexByte : code) {
            codeString += hexByte + " ";
            testString += "[" + intToHexString(i) + "]: " + hexByte + " ";
            i++;
        }

        // copy code to clipboard
        if (verbose) { copyStringToClipboard(codeString); }

        codeString = messagesString() + "Code:\n" + codeString;
        if (verbose) { System.out.println("\n" + testString); }
        return codeString;
    }

    private static String messagesString() {
        String messagesString = "\n";
        if (messages.size() > 0) {
            messagesString = "\nMessages:\n";
            for (String message : messages) {
                messagesString += "--" + message + "\n";
            }
        }
        return messagesString;
    }

    private static void newMessage() {
        if (whileLoopStart.size() > 0) {
            messages.add("Warning: Infinite loop detected.");
        }
        else {
            messages.add("Info: Always true conditional found");
        }
    }

    private static void copyStringToClipboard(String str) {
        StringSelection stringSelection = new StringSelection(str);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    private static void giveVariablesAddresses() {
        for (StaticTableData i : staticTable) {
            String varLocation = intToHexString(currentMemoryLocation);
            for (String j : code) {
                if (j.equals(i.getTemp())) {
                    code.set(code.indexOf(j), varLocation);
                }
            }
            currentMemoryLocation++;
        }
    }

    private static void setJumpDistances() {
        for (int i = 0; i < jumpTable.size(); i++) {
            for (String j : code) {
                if (j.equals("J" + i)) {
                    code.set(code.indexOf(j), intToHexString(jumpTable.get("J" + i)));
                }
            }
        }
    }
    
}
