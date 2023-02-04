package src;

public class Token {

    private String name;
    private String value;
    private int line;
    private int column;

    public Token(String name, String value, int line, int column) {
        this.name = name;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public String getName() {
        return name;
    }
    public String getValue() {
        return value;
    }
    public int getLine() {
        return line;
    }
    public int getColumn() {
        return column;
    }
}
