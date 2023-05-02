public class Position {

    private int line;
    private int column;
    private int fileLine;

    public Position(int line, int column) {
        this.line = line;
        this.column = column;
        this.fileLine = Compiler.fileLine;
    }

    //getters
    public int getLine() { return line; }
    public int getColumn() { return column; }
    public int getFileLine() { return fileLine; }

    //setters
    public void setLine(int line) { this.line = line; }
    public void setColumn(int column) { this.column = column; }

    public String toString() {
        return "Line: " + line + " Column: " + column + "";
    }
}
