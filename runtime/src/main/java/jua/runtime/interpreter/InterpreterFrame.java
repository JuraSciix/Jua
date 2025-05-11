package jua.runtime.interpreter;

public final class InterpreterFrame {

    private InterpreterFrame caller;
    private int functionId;
    private int cp;
    private int regBase;

    public void setCaller(InterpreterFrame caller) {
        this.caller = caller;
    }

    public void setFunctionId(int functionId) {
        this.functionId = functionId;
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

    public int getFunctionId() {
        return functionId;
    }

    public int getCP() {
        return cp;
    }

    public int getRegBase() {
        return regBase;
    }
}
