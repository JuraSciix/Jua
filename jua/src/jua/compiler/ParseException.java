package jua.compiler;

@Deprecated
public class ParseException extends RuntimeException {

    public final int position;

    public ParseException(String message, int position) {
        super(message);
        this.position = position;
    }
}
