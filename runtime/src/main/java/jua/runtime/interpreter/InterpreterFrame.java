package jua.runtime.interpreter;

import jua.runtime.Function;

public final class InterpreterFrame {

    private InterpreterFrame caller;
    private Function function;
    private int cp;

    public void setCaller(InterpreterFrame caller) {
        this.caller = caller;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public void setCP(int cp) {
        this.cp = cp;
    }

    public InterpreterFrame getCaller() {
        return caller;
    }

    public Function getFunction() {
        return function;
    }

    public int getCP() {
        return cp;
    }
}
