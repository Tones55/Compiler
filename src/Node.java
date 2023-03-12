public class Node {

    private static String name;
    private static Token tokenPointer;

    public Node(String label , Token token) {
        name = label;
        tokenPointer = token;
    }

    public static Node createNode(String label , Token token) {
        return new Node(label , token);
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