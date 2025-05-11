package jua.runtime.interpreter;

import jua.runtime.Function;

public final class InterpreterFrame {

    private InterpreterFrame caller;
    private Function function;
    private int cp;
    private int regBase;

    public void setCaller(InterpreterFrame caller) {
        this.caller = caller;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public void setCP(int cp) {
        this.cp = cp;
    }

    public void setRegBase(int regBase) {
        if (regBase < 0) {
            throw new IllegalArgumentException("Negative reg base: " + regBase);
        }
        this.regBase = regBase;
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

    public int getRegBase() {
        return regBase;
    }
}
