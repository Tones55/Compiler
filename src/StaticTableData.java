public class StaticTableData {
    
    private String temp;
    private String var;
    private String scope;
    private String offset;

    public StaticTableData(String temp, String var, String scope, String offset) {
        this.temp = temp;
        this.var = var;
        this.scope = scope;
        this.offset = offset;
    }

    // Getters
    public String getTemp() {
        return temp;
    }

    public String getVar() {
        return var;
    }

    public String getScope() {
        return scope;
    }

    public String getOffset() {
        return offset;
    }

    // Setters
    public void setTemp(String temp) {
        this.temp = temp;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }
}
