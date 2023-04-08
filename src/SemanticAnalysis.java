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
    private static ArrayList<DefaultMutableTreeNode> backTrackNodes = new ArrayList<DefaultMutableTreeNode>();
    private static Pattern character = Pattern.compile("[a-z]|\\s"); // can only be found in quotes

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
            currentCSTNode = e.nextElement();
            //System.out.println(currentASTNode);
            switch (currentCSTNode.toString().split(" ")[0]) {
                case "<Block>":
                    addBlock();
                    break;
                case "}":
                    //moveUp();
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
        addNode();
        backTrackNodes.add((DefaultMutableTreeNode) currentASTNode.getParent());
    }
    private static void moveUp() {
        currentASTNode = backTrackNodes.get(backTrackNodes.size() - 1);
        backTrackNodes.remove(backTrackNodes.size() - 1);
    }
    private static void addPrintStatement() {
        addNode();
        backTrackNodes.add((DefaultMutableTreeNode) currentASTNode.getParent());
        skipNodes(4);
        decipherExpression();
        moveUp();
    }
    private static void addAssignmentStatement() {
        addNode();
        backTrackNodes.add((DefaultMutableTreeNode) currentASTNode.getParent());
        skipNodes(1);
        addIdentifier();
        skipNodes(3);
        decipherExpression();
        moveUp();
    }
    private static void addVariableDecleration() {
        addNode();
        backTrackNodes.add((DefaultMutableTreeNode) currentASTNode.getParent());
        skipNodes(2);
        addNode();
        currentASTNode = (DefaultMutableTreeNode) currentASTNode.getParent();
        skipNodes(3);
        addNode();
        moveUp();
    }
    private static void addWhileStatement() {
        addNode();
        backTrackNodes.add((DefaultMutableTreeNode) currentASTNode.getParent());
        skipNodes(2);
        findBooleanType();
        moveUp();
    }
    private static void addIfStatement() {
        addNode();
        backTrackNodes.add((DefaultMutableTreeNode) currentASTNode.getParent());
        skipNodes(2);
        findBooleanType();
        moveUp();
    }
    private static void addStringExpression() {
        addNode();
        backTrackNodes.add((DefaultMutableTreeNode) currentASTNode.getParent());
        skipNodes(4);
        while (!currentCSTNode.toString().split(" ")[0].equals("\"")) {
            Matcher m = character.matcher(currentCSTNode.toString().split(" ")[0]);
            if(m.matches()) {
                addNode();
                currentASTNode = (DefaultMutableTreeNode) currentASTNode.getParent();
            }
            skipNodes(1);
        }
        moveUp();
    }
    private static void addIntegerExpression() {
        skipNodes(2);
        addNode();
        backTrackNodes.add((DefaultMutableTreeNode) currentASTNode.getParent());
        skipNodes(1);
        if(currentCSTNode.toString().split(" ")[0].equals("<Integer_Operator>")) {
            skipNodes(5);
            addNode();
        }
        moveUp();
    }
    private static void addBooleanExpression() {
        DefaultMutableTreeNode tempNode = new DefaultMutableTreeNode("temp");
        currentASTNode.add(tempNode);
        backTrackNodes.add((DefaultMutableTreeNode) currentASTNode.getParent());
        currentASTNode = tempNode;
        skipNodes(1);
        addNode();
        skipNodes(2);
        if(currentCSTNode.toString().split(" ")[0].equals("==")) {
            currentASTNode.setUserObject("Is_Equal");
        }
        else {
            currentASTNode.setUserObject("Is_Not_Equal");
        }
        skipNodes(1);
        addNode();
        moveUp();
    }
    private static void addBooleanValue() {
        skipNodes(2);
        addNode();
        backTrackNodes.add((DefaultMutableTreeNode) currentASTNode.getParent());
        moveUp();
    }
    private static void addIdentifier() {
        skipNodes(2);
        addNode();
        currentASTNode = (DefaultMutableTreeNode) currentASTNode.getParent();
    }

    //adds a node to the AST
    private static void addNode() {
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(currentCSTNode.toString().split(" ")[0]);
        currentASTNode.add(newNode);
        currentASTNode = newNode;
        if (verbose) {System.out.println("Added: " + currentCSTNode.toString().split(" ")[0]);}
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
        switch (currentCSTNode.toString().split(" ")[0]) {
            case "(":
                addBooleanExpression();
                break;
            default:
                addBooleanValue();
                break;
        }
    }
    // used to skip num nodes in the CST
    private static void skipNodes(int num) {
        for (int i = 0; i < num; i++) {
            currentCSTNode = e.nextElement();
        }
    }
}
