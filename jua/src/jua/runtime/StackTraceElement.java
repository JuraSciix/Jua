package jua.runtime;

import java.util.Objects;

public final class StackTraceElement {

    private final String function;

    private final String fileName;

    private final int lineNumber;

    public StackTraceElement(String function, String fileName, int lineNumber) {
        this.function = Objects.requireNonNull(function);
        this.fileName = Objects.requireNonNull(fileName);
        this.lineNumber = lineNumber;
    }

    public String getFunction() { return function; }

    public String getFileName() { return fileName; }

    public int getLineNumber() { return lineNumber; }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, lineNumber, function);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != getClass()) return false;
        StackTraceElement e = (StackTraceElement) o;
        return function.equals(e.function) && (lineNumber == e.lineNumber) && fileName.equals(e.fileName);
    }

    @Override
    public String toString() {
        return getFunction() + "(" + getFileName() + ":" + getLineNumber() + ")";
    }
}
