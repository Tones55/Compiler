public class VariableInfo {
    
    private String type;
    private boolean isInitialized;
    private boolean isUsed;

    public VariableInfo(String type) {
        this.type = type;
        this.isInitialized = false;
        this.isUsed = false;
    }

    public String getType() {
        return type;
    }
    public boolean isInitialized() {
        return isInitialized;
    }
    public boolean isUsed() {
        return isUsed;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }
    public void setUsed(boolean isUsed) {
        this.isUsed = isUsed;
    }
    public String toString() {
        return "Type: " + type + " Initialized: " + isInitialized + " Used: " + isUsed;
    }
}
