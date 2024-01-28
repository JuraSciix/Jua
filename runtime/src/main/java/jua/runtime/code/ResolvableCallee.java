package jua.runtime.code;

import jua.runtime.Function;

public class ResolvableCallee {

    int utf8;

    Function resolved;

    public ResolvableCallee(int utf8) {
        this.utf8 = utf8;
    }

    public boolean isResolved() {
        return resolved != null;
    }

    public int getUtf8() {
        return utf8;
    }

    public Function getResolved() {
        return resolved;
    }

    public void setResolved(Function resolved) {
        this.resolved = resolved;
    }
}
