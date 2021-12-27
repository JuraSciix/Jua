package jua.parser;

public class ParseException extends Exception {

    public final Tree.Position position;

    public ParseException(String message, Tree.Position position) {
        super(message);
        this.position = position;
    }
}
