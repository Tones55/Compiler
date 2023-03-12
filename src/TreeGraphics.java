/*
 * There is a lot of code in this file copied/repurposed from a java demo.
 * The original file can be found at:
 * http://java.sun.com/docs/books/tutorial/uiswing/components/tree.html
 */

import javax.swing.tree.DefaultMutableTreeNode;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;

public class TreeGraphics extends JPanel implements TreeSelectionListener {

    private JTree tree;

    //Optionally play with line styles.  Possible values are
    //"Angled" (the default), "Horizontal", and "None".
    private static boolean playWithLineStyle = false;
    private static String lineStyle = "Horizontal";
    
    //Optionally set the look and feel.
    private static boolean useSystemLookAndFeel = true;

    public TreeGraphics(DefaultMutableTreeNode CST) {
        super(new GridLayout(1,1));

        //Create a tree that allows one selection at a time.
        tree = new JTree(CST);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }

        //Listen for when the selection changes.
        tree.addTreeSelectionListener(this);

        if (playWithLineStyle) {
            System.out.println("line style = " + lineStyle);
            tree.putClientProperty("JTree.lineStyle", lineStyle);
        }

        //Create the scroll pane and add the tree to it.
        JScrollPane treeView = new JScrollPane(tree);

        Dimension minimumSize = new Dimension(600, 100);
        treeView.setMinimumSize(minimumSize);

        //Add the pane
        add(treeView);
    }

    public static void createAndShowGUI(DefaultMutableTreeNode CST) {
        if (useSystemLookAndFeel) {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Couldn't use system look and feel.");
            }
        }

        //Create and set up the window.
        JFrame frame = new JFrame("Program CST");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new TreeGraphics(CST));

        frame.setPreferredSize(new Dimension(600, 400));
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - 300 , dim.height / 2 - 200);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public void valueChanged(TreeSelectionEvent e) {
        // not needed
    }

}
