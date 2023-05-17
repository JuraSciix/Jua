package jua.runtime;

import java.io.PrintStream;
import java.util.Objects;

public final class StackTraceElement {

    /** Название исполняемого файла. */
    public final String fileName;

    /** Название исполняемой функции. */
    public final String function;

    /** Номер исполняемой строки. Равно {@code -1}, если функция нативная. */
    public final int lineNumber;

    public StackTraceElement(String fileName, String function, int lineNumber) {
        this.fileName = Objects.requireNonNull(fileName, "file name");
        this.function = Objects.requireNonNull(function, "function");
        this.lineNumber = lineNumber;
    }

    public void print(PrintStream output) {
        output.print(function);
        output.print('(');
        output.print(fileName);
        output.print(':');
        output.print(lineNumber);
        output.print(')');
    }

    @Override
    public int hashCode() {
        return ((fileName.hashCode() * 17 + function.hashCode()) * 17 + Integer.hashCode(lineNumber));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != getClass()) return false;
        StackTraceElement e = (StackTraceElement) o;
        return (lineNumber == e.lineNumber) && fileName.equals(e.fileName) && function.equals(e.function);
    }
}
