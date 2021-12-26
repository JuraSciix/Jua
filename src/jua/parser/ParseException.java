package jua.parser;

import jua.parser.tree.Position;

public class ParseException extends Exception {

    public final Position position;

    public ParseException(String message, Position position) {
        super(message);
        this.position = position;
    }
}
