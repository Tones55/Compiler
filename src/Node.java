public class Node {

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