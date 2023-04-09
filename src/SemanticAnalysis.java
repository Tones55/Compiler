import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SemanticAnalysis {

    private static boolean verbose = true;
    private static boolean hasError = false;
    private static DefaultMutableTreeNode cst;
    private static Enumeration<TreeNode> e;
    private static DefaultMutableTreeNode ast;
    private static TreeNode currentCSTNode;
    private static DefaultMutableTreeNode currentASTNode;
    private static Pattern character = Pattern.compile("[a-z]|\\s");
    private static ArrayList<DefaultMutableTreeNode> blockBackTrackNodes = new ArrayList<DefaultMutableTreeNode>();

    public static String doSemanticAnalysis(DefaultMutableTreeNode root) {
        if (root == null) {
            hasError = true;
        }

        if (!hasError) {
            System.out.println("Semantic Analysis: Doing Semantic Analysis...");
            cst = root;
            e = cst.preorderEnumeration();
            skipNodes(2);
            ast = new DefaultMutableTreeNode(currentCSTNode.toString());
            currentASTNode = ast;
            //blockBackTrackNodes.add(currentASTNode);
            if (verbose) {System.out.println("Added: " + currentCSTNode.toString().split(" ")[0]);}
            generateAST();
            generateSymbolTable();

            printAST();
        }
        else {
            System.out.println("Semantic Analysis: Skipping Semantic Analysis due to Parse Error");
        }
        return "Semantic Analysis";
    }

    private static void printAST() {
        System.out.println("\nPrinting AST: ");
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
        while (e.hasMoreElements()) {
            skipNodes(1);
            //System.out.println(currentASTNode);
            switch (currentCSTNode.toString().split(" ")[0]) {
                case "<Block>":
                    addBlock();
                    break;
                case "}":
                    if (blockBackTrackNodes.size() > 0) {
                        System.out.println("moving up AST");
                        currentASTNode = blockBackTrackNodes.get(blockBackTrackNodes.size() - 1);
                        blockBackTrackNodes.remove(blockBackTrackNodes.size() - 1);
                    }
                    else {
                        System.out.println("End of program block");
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
    private static void generateSymbolTable() {
        
    }
    private static void addBlock() {
        blockBackTrackNodes.add((DefaultMutableTreeNode) currentASTNode.getParent());
        addNode();
    }
    private static void addPrintStatement() {
        addNode();
        DefaultMutableTreeNode backTrackNode = (DefaultMutableTreeNode) currentASTNode.getParent();
        skipNodes(4);
        decipherExpression();
        currentASTNode = backTrackNode;
    }
    private static void addAssignmentStatement() {
        addNode();
        DefaultMutableTreeNode backTrackNode = (DefaultMutableTreeNode) currentASTNode.getParent();
        skipNodes(1);
        addIdentifier();
        skipNodes(2);
        decipherExpression();
        currentASTNode = backTrackNode;
    }
    private static void addVariableDecleration() {
        addNode();
        DefaultMutableTreeNode backTrackNode = (DefaultMutableTreeNode) currentASTNode.getParent();
        skipNodes(2);
        addNode();
        currentASTNode = (DefaultMutableTreeNode) currentASTNode.getParent();
        skipNodes(3);
        addNode();
        currentASTNode = backTrackNode;
    }
    private static void addWhileStatement() {
        addNode();
        skipNodes(2);
        findBooleanType();
        addBlock();
    }
    private static void addIfStatement() {
        addNode();
        skipNodes(2);
        findBooleanType();
        addBlock();
    }
    private static void addStringExpression() {
        addNode();
        DefaultMutableTreeNode backTrackNode = (DefaultMutableTreeNode) currentASTNode.getParent();
        skipNodes(4);
        while (!currentCSTNode.toString().split(" ")[0].equals("\"")) {
            Matcher m = character.matcher(currentCSTNode.toString().split(" ")[0]);
            if(m.matches()) {
                addNode();
                currentASTNode = (DefaultMutableTreeNode) currentASTNode.getParent();
            }
            skipNodes(1);
        }
        currentASTNode = backTrackNode;
    }
    private static void addIntegerExpression() {
        DefaultMutableTreeNode backTrackNode;
        DefaultMutableTreeNode digitNode;
        skipNodes(2);
        digitNode = new DefaultMutableTreeNode(currentCSTNode.toString().split(" ")[0]);
        System.out.println("----------------------------------------------------------- digit");
        skipNodes(1);
        if(currentCSTNode.toString().split(" ")[0].equals("<Integer_Operator>")) {
            DefaultMutableTreeNode sumNode = new DefaultMutableTreeNode("Sum_Of");
            currentASTNode.add(sumNode);
            currentASTNode = sumNode;
            backTrackNode = (DefaultMutableTreeNode) currentASTNode.getParent();
            currentASTNode.add(digitNode);
            skipNodes(3);
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
        skipNodes(2);
        decipherExpression();
        skipNodes(1);
        if(currentCSTNode.toString().split(" ")[0].equals("==")) {
            currentASTNode.setUserObject("Is_Equal");
        }
        else if(currentCSTNode.toString().split(" ")[0].equals("!=")) {
            currentASTNode.setUserObject("Is_Not_Equal");
        }
        else {
            System.out.println("\nError: Invalid Boolean Operator\n");
        }
        skipNodes(2);
        decipherExpression();
        skipNodes(1);
        currentASTNode = backTrackNode;
    }
    private static void addBooleanValue() {
        skipNodes(1);
        addNode();
        skipNodes(1);
        currentASTNode = (DefaultMutableTreeNode) currentASTNode.getParent();
    }
    private static void addIdentifier() {
        skipNodes(2);
        addNode();
        skipNodes(1);
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
        skipNodes(1);
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
    private static void skipNodes(int num) {
        for (int i = 0; i < num; i++) {
            currentCSTNode = e.nextElement();
            System.out.println("Current CST Node: " + currentCSTNode.toString().split(" ")[0]);
        }
    }
    //adds a node to the AST
    private static void addNode() {
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(currentCSTNode.toString().split(" ")[0]);
        currentASTNode.add(newNode);
        currentASTNode = newNode;
        if (verbose) {System.out.println("Added: " + currentCSTNode.toString().split(" ")[0]);}
    }
}
