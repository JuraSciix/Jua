package jua.compiler;

public abstract class Log {

    /** Счетчик ошибок. */
    int errorCounter = 0;

    public void error(Source source, int pos, String msg, Object... args) {
        error(source, pos, String.format(msg, args));
    }

    public abstract void error(Source source, int pos, String msg);

    @Deprecated
    public boolean hasErrors() { return errorCounter > 0; }
}
