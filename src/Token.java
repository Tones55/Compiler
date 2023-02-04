package src;

public class Token {

    private String name; //keyword,identifier,symbol,digit,character
    private String value; //the actual value of the token
    private int line;
    private int column;

    public Token(String name, String value, int line, int column) {
        this.name = name;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    //getters
    public String getName() { return name; }
    public String getValue() { return value; }
    public int getLine() { return line; }
    public int getColumn() { return column; }

    //setters
    public void setName(String name) { this.name = name; }
    public void setValue(String value) { this.value = value; }
    public void setLine(int line) { this.line = line; }
    public void setColumn(int column) { this.column = column; }

    public String toString() {
        return "Found Token: " + name + " at location " + "Line: " + line + " Column: " + column;
    }
}