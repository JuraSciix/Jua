package jua.compiler;

public class Log {

    // todo: Finish class

    public void error(int pos, String msg) {
        throw new ParseException(msg, pos);
    }

    public void error(int pos, String fmt, Object... args) {
        throw new ParseException(String.format(fmt, args), pos);
    }
}
