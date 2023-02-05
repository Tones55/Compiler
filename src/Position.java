package src;

public class Position {

    private int line;
    private int column;

    public Position(int line, int column) {
        this.line = line;
        this.column = column;
    }

    //getters
    public int getLine() { return line; }
    public int getColumn() { return column; }

    //setters
    public void setLine(int line) { this.line = line; }
    public void setColumn(int column) { this.column = column; }

    public String toString() {
        return "Line: " + line + " Column: " + column + "";
    }
}
