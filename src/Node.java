public class Node {

    /*
     * currently unused, there was an issue when using this object
     * pointers did not work as expected when using this object inside DefaultMutableTreeNode
     * instead of usign this object a string is used to represent a token
     * the string is formatted as follows: "<token.value> <SPACE> <token.position.line> <SPACE> <token.position.column> <SPACE> <token.name>"
     * an example would be: "int 12 1 keyword" or "7 5 16 digit"
     * the data is then extracted using String.split and Integer.parseInt
     * if the issue is resolved this object will be used again
     */


    private static String name;
    private static Token tokenPointer;

    public Node(String label , Token token) {
        name = label;
        tokenPointer = token;
    }

    public Token getToken() {
        return tokenPointer;
    }

    public String getName () {
        return name;
    }

    public String toString() {
        return name;
    }
}