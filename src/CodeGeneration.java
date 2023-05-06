import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class CodeGeneration {

    private static boolean verbose = true;
    private static boolean hasError;
    private static ArrayList<String> code;
    private static ArrayList<StaticTableData> staticTable;
    private static ArrayList<String> jumpTable;
    private static ArrayList<Integer> scopeBacktrackDepths;
    private static DefaultMutableTreeNode ast;
    private static DefaultMutableTreeNode symbolTable;
    private static DefaultMutableTreeNode currentASTNode;
    private static DefaultMutableTreeNode currentScope;
    private static Enumeration<TreeNode> scopEnumeration;
    private static Enumeration<TreeNode> astEnumeration;
    private static Hashtable<DefaultMutableTreeNode , Integer> scopeNames;
    private static Hashtable<String , String> stringsInHeap;
    private static int currentMemoryLocation;
    private static int heapDepth;
    private static String[] booleansInMemory = {"00" , "00"};

    public static String doCodeGeneration(DefaultMutableTreeNode[] roots) {
        if (roots == null) {
            return "Skipped Code Generation due to compilation error";
        }

        initializeVariables(roots);
        generateCode();

        return codeArrayToString();
    }

    private static void initializeVariables(DefaultMutableTreeNode[] roots) {
        hasError = false;
        code = new ArrayList<>();
        initalizeCode();
        staticTable = new ArrayList<>();
        jumpTable = new ArrayList<>();
        ast = roots[0];
        symbolTable = roots[1];
        currentASTNode = ast;
        currentScope = symbolTable;
        scopEnumeration = symbolTable.preorderEnumeration();
        astEnumeration = ast.preorderEnumeration();
        scopeNames = new Hashtable<>();
        initializeScopeNames();
        currentMemoryLocation = 0x00;
        heapDepth = 0xFF;
        scopeBacktrackDepths = new ArrayList<>();
        stringsInHeap = new Hashtable<>();
    }

    private static void initalizeCode() {
        // initialize code array with 256 bytes of 00
        for (int i = 0; i < 256; i++) {
            code.add("00");
        }
    }

    private static void initializeScopeNames() {
        // initialize scope names
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
                System.out.println("Code Generation Error: Out of Memory");
            } 
            if (hasError) { return; }

            // makes sure that current scope can backtrack when a block is exited
            boolean correctScope = false;
            while (!correctScope) {
                if (scopeBacktrackDepths.size() > 0) {
                    if (currentASTNode.getLevel() < scopeBacktrackDepths.get(scopeBacktrackDepths.size() - 1)) {
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
            nextASTNode();
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
    }

    private static void addToStaticDataTable(String var , boolean pointer) {
        staticTable.add(new StaticTableData("T" + staticTable.size() , var , scopeNames.get(currentScope) , staticTable.size() , pointer));
    }

    private static void enterBlock() {
        // go to proper scope
        currentScope = (DefaultMutableTreeNode) scopEnumeration.nextElement();
        scopeBacktrackDepths.add(currentASTNode.getLevel());
    }

    private static void exitBlock() {
        // go to parent scope
        currentScope = (DefaultMutableTreeNode) currentScope.getParent();
        scopeBacktrackDepths.remove(scopeBacktrackDepths.size() - 1);
    }

    private static void Print_Statement() { 
        // 01 in x reg = print int in y reg
        // 02 in x reg = print string stored at address in y reg
        // op code FF to do a system call

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
                String location = getVarLocation(currentASTNode.toString().split(" ")[0], scopeNames.get(currentScope));
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
                }
                // if the variable is an int
                else {
                    // load x reg with 01
                    addCodeToMemory("A2");
                    addCodeToMemory("01");

                    // load y reg with data at location
                    addCodeToMemory("AC");
                    addCodeToMemory(location);
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
            // load x reg with 01
            addCodeToMemory("A2");
            addCodeToMemory("01");

            // add sum code
            String sumLocation = Sum_Of(true);

            // store sum in memory
            addCodeToMemory("8D");
            addCodeToMemory(sumLocation);
            addCodeToMemory("00");

            // load y reg with sum
            addCodeToMemory("AC");
            addCodeToMemory(sumLocation);
            addCodeToMemory("00");
        }
        // print a boolean
        else {
            // print true
            if(currentASTNode.toString().split(" ")[0].equals("true")) {
                if(booleansInMemory[0].equals("00")) {
                    addBooleanValueToMemory("true");
                }
                // load x reg with 02
                addCodeToMemory("A2");
                addCodeToMemory("02");

                // load y reg with pointer to string
                addCodeToMemory("A0");
                addCodeToMemory(booleansInMemory[0]);
            }
            // print false
            else {
                if(booleansInMemory[1].equals("00")) {
                    addBooleanValueToMemory("false");
                }
                // load x reg with 02
                addCodeToMemory("A2");
                addCodeToMemory("02");

                // load y reg with pointer to string
                addCodeToMemory("A0");
                addCodeToMemory(booleansInMemory[0]);
            }
        }
        // system call
        addCodeToMemory("FF");
    }

    private static void Assignment_Statement() {
        // for sum go to Sum_Of() then do int steps
        // for int
        // load acc with digit in hex: A9 ??
        // store acc in mem temp: 8D T? XX
        // for string
        // write data into heap
        // store the pointer to the heap in the temp: A9 [Start of string in heap] 8D T? XX

        // get the temp address for the variable
        nextASTNode();
        String tempLocation = getVarLocation(currentASTNode.toString().split(" ")[0], scopeNames.get(currentScope));

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
            String tempLocationB = getVarLocation(currentASTNode.toString().split(" ")[0], scopeNames.get(currentScope));

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
        // add to static table
        // for int
        // load acc with 0: A9 00
        // sta acc in mem temp: 8D T? XX
        // for string
        // just add to static table
        // for boolean
        // load acc with 0: A9 00
        // sta acc in mem temp: 8D T? XX

        // add variable to static table
        nextASTNode();
        String varType = currentASTNode.toString().split(" ")[0];
        nextASTNode();

        if(varType.equals("string")) {
            addToStaticDataTable(currentASTNode.toString().split(" ")[0] , true);
        }
        else {
            addToStaticDataTable(currentASTNode.toString().split(" ")[0] , false);
        }

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
        // if condition is a boolval then BNE boolval compared to 1
    }

    private static void If_Statement() {
        // if condition is a boolval then compare that to 1
        // if false will jump to end of if statement
        // if true will continue to next statement
        // if condition thengo to Boolean_Expression()

        nextASTNode();
        if (currentASTNode.toString().charAt(0) == '<') {
            Boolean_Expression();
        }
        else {
            // boolval
        }
    }

    private static String Sum_Of(boolean first) {
        // will leave result in accumulator

        // load acc with digit: A9 ??
        // check next operand
        // if digit put acc in mem, load acc with digit and ADC with that location: 6D ?? 00
        // if var ADC with it: 6D T? XX
        // if sum of then recursion

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
                addCodeToMemory(getVarLocation(currentASTNode.toString().split(" ")[0] , scopeNames.get(currentScope))); 
            }
        }
        // if the next operand is another Sum_Of
        else {
            // do recursion
            Sum_Of(false);
        }
        return "00";
    }

    private static void Boolean_Expression() {
        // store comparison type in isEquivalence
        // check first operand
        // if boolval load x reg with 0 or 1
            // check next operand
                // if boolval load acc with 0 or 1 and store it at 00
                    // compare x to 00
                // if var compare x reg to var location

        // if digit load x reg with digit
            // check next operand
                // if digit load acc with digit and store it at 00
                    // compare x to 00
                // if var compare x reg to var location

        // if var save its location in varLocation
        // check vars type
            // check next operand
                // if digit load it to x reg
                    // compare x reg to varLocation
                // if boolean load it to x reg
                    // compare x reg to varLocation
                // if string literal
                    // check stringsInHeap for it
                        // if it is there add that address to x reg
                            // compare x reg to varLocation
                        // if it is not there then condition is false and we can skip the block

        // if string literal
            // check stringsInHeap for it
                // if it is there save the address in strLocation and the string in tempString
                    // if next operand is a string literal compare it to tempString
                        // if true then gen code otherwise skip to end of block
                    // if next operand is a var then add str location to x reg and compare to var location
                // if it is not there add it to a tempString var and go to the next operand
                    // if next operand is a string literal compare it to tempString
                    // if next operand is a var then condition is false and we can skip the block
        
        boolean isEquivalence = false;
        if (currentASTNode.toString().split(" ")[0].equals("<Is_Equal>")) {
            isEquivalence = true;
        }
        nextASTNode();

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
        code.set(currentMemoryLocation, hexCode);
        currentMemoryLocation++;
    }

    private static void nextASTNode() {
        if (astEnumeration.hasMoreElements()) {
            currentASTNode = (DefaultMutableTreeNode) astEnumeration.nextElement();
            System.out.println("Current AST Node: " + currentASTNode.toString());
        }
    }

    private static void addStringToHeap(String str) {
        heapDepth -= str.replaceAll("\"", "").length();
        String[] hexArray = stringToAscii(str);
        int heapPointer = heapDepth;

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

    private static String getVarLocation(String var , int scope) {
        String tempLocation = "";
        for(int i = 0; i < staticTable.size(); i++) {
            if (staticTable.get(i).getVar().equals(var)) {
                if (staticTable.get(i).getScope() == scope) {
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
        if (verbose) { System.out.println("\n" + testString); }
        return codeString;
    }
}
