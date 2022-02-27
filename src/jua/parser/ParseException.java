package jua.parser;

public class ParseException extends Exception {

    public final int position;

    public ParseException(String message, int position) {
        super(message);
        this.position = position;
    }
}
