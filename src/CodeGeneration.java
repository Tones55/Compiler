import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class CodeGeneration {

    private static boolean verbose = true;
    private static boolean hasError;
    private static ArrayList<String> code;
    private static ArrayList<StaticTableData> staticTable;
    private static ArrayList<String> jumpTable;
    private static DefaultMutableTreeNode ast;
    private static DefaultMutableTreeNode symbolTable;
    private static DefaultMutableTreeNode currentASTNode;
    private static DefaultMutableTreeNode currentScope;
    private static Enumeration<TreeNode> scopEnumeration;
    private static Enumeration<TreeNode> astEnumeration;
    private static int currentMemoryLocation;
    private static int heapDepth;
    private static int scope;

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
        currentMemoryLocation = 0x00;
        heapDepth = 0xFF;
        scope = 0;
    }

    private static void initalizeCode() {
        // initialize code array with 256 bytes of 00
        for (int i = 0; i < 256; i++) {
            code.add("00");
        }
    }

    private static void generateCode() {
        while(astEnumeration.hasMoreElements()) {
            if (currentMemoryLocation > heapDepth) {
                hasError = true;
            } 
            if (hasError) { return; }

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

    private static void addToStaticDataTable(String var) {
        staticTable.add(new StaticTableData("T" + staticTable.size() + " 00", var , scope , staticTable.size()));
    }

    private static void enterBlock() {
        // go to proper scope
        currentScope = (DefaultMutableTreeNode) scopEnumeration.nextElement();
    }

    private static void exitBlock() {
        // go to parent scope
        currentScope = (DefaultMutableTreeNode) currentScope.getParent();
    }

    private static void Print_Statement() {
        // op code FF 
        // 01 in x reg = print int in y reg
        // 02 in x reg = print string stored at address in y reg
        addCodeToMemory("FF");

        nextASTNode();
        if (currentASTNode.toString().length() == 1) {
            if (Character.isDigit(currentASTNode.toString().charAt(0))) {
                // load x reg with 01
                addCodeToMemory("A2");
                addCodeToMemory("01");

                // load y reg with digit
                addCodeToMemory("A0");
                addCodeToMemory("0" + currentASTNode.toString());

                // system call
                addCodeToMemory("FF");
            }
            else {

            }
        }
        else if (currentASTNode.toString().charAt(0) == '"') {
            heapDepth -= currentASTNode.toString().replaceAll("\"", "").length();
            addStringToHeap(currentASTNode.toString());

            // load x reg with 02
            addCodeToMemory("A2");
            addCodeToMemory("02");

            // load y reg with pointer to string
            addCodeToMemory("A0");
            addCodeToMemory(Integer.toHexString(heapDepth + 1).toUpperCase());

            System.out.println(Integer.toHexString(heapDepth + 1).toUpperCase());
        }
        else {
            // Sum_Of();
        }
    }

    private static void Assignment_Statement() {
        // for sum go to Sum_Of() then do int steps
        // for int
        // load acc with digit in hex: A9 ??
        // store acc in mem temp: 8D T? XX
        // for string
        // write data into heap
        // store the pointer to the heap in the temp: A9 [Start of string in heap] 8D T? XX
    }

    private static void Variable_Decleration() {
        // add to static table
        // for int
        // load acc with 0: A9 00
        // sta acc in mem temp: 8D T? XX
    }

    private static void While_Statement() {
        // if condition is a boolval then BNE boolval compared to 1
    }

    private static void If_Statement() {
        // if condition is a boolval then BNE boolval compared to 1
    }

    private static void string() {
        // delete later ???
    }

    private static void Sum_Of() {
        // load acc with digit: A9 ??
        // check next operand
        // if digit put it in mem and ADC with that location: 6D ?? 00
        // if var ADC with it: 6D T? XX
    }

    private static void Boolean_Expression() {

    }

    private static void Boolean_Value() {
        nextASTNode();
        if (currentASTNode.toString().equals("true")) {
            // 1 for true
        } 
        else {
            // 0 for false
        }
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
        }
    }

    private static void addStringToHeap(String str) {
        String[] hexArray = stringToAscii(str);
        int heapPointer = heapDepth;

        for (String hexByte : hexArray) {
            System.out.println("ADDED TO HEAP: " + hexByte + " AT: " + heapPointer + "");
            if(!code.get(heapPointer).equals("00")) {
                hasError = true;
                return;
            }
            code.set(heapPointer, hexByte);
            heapPointer++;
        }
        heapDepth--;
        System.out.println(Integer.toHexString(heapDepth));
    }

    private static String[] stringToAscii(String str) {
        str = str.substring(1, str.length() - 1);
        str = str.replaceAll("_", " ");

        if (verbose) { System.out.println(str); }

        String[] hexArray = new String[str.length() + 1];
        for (int i = 0; i < str.length(); i++) {
            int asciiValue = (int) str.charAt(i);
            String hexValue = Integer.toHexString(asciiValue);
            hexArray[i] = hexValue.toUpperCase();
        }
        hexArray[str.length()] = "00";
        return hexArray;
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
            testString += "[" + Integer.toHexString(i) + "]: " + hexByte + " ";
            i++;
        }
        if (verbose) { System.out.println("\n" + testString); }
        return codeString;
    }
}
