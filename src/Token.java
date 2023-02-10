package src;

public class Token {

    private String name; //keyword,identifier,symbol,digit,character
    private String value; //the actual value of the token
    private Position position;

    public Token(String name, String value, Position position) {
        this.name = name;
        this.value = value;
        this.position = position;
    }

    //getters
    public String getName() { return name; }
    public String getValue() { return value; }
    public Position getPosition() { return position; }

    //setters
    public void setName(String name) { this.name = name; }
    public void setValue(String value) { this.value = value; }
    public void setPosition(Position position) { this.position = position; }

    public String toString() {
        return "Found Token: " + name + " [" + value + "]" + " at location " + position.toString();
    }
}