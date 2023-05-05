public class StaticTableData {
    
    private String temp;
    private String var;
    private int scope;
    private int offset;
    private boolean pointer;

    public StaticTableData(String temp, String var, int scope, int offset , boolean pointer) {
        this.temp = temp;
        this.var = var;
        this.scope = scope;
        this.offset = offset;
        this.pointer = pointer;
    }

    // Getters
    public String getTemp() {
        return temp;
    }

    public String getVar() {
        return var;
    }

    public int getScope() {
        return scope;
    }

    public int getOffset() {
        return offset;
    }

    public boolean getPointer() {
        return pointer;
    }

    // Setters
    public void setTemp(String temp) {
        this.temp = temp;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setPointer(boolean pointer) {
        this.pointer = pointer;
    }

    public String toString() {
        return "Location: " + temp + " Var: " + var + " Scope: " + scope + " Offset: " + offset;
    }
}
