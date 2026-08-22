package jua.vm;

public final class InterpreterFrame {

    private InterpreterFrame caller;
    private int functionId;
    private int cp;
    private int offset;
    private int stackPointer;
    private int top;

    public void setCaller(InterpreterFrame caller) {
        this.caller = caller;
    }

    public void setFunctionId(int functionId) {
        this.functionId = functionId;
    }

    public void setCP(int cp) {
        this.cp = cp;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public void stackPointer(int stackPointer) {
        this.stackPointer = stackPointer;
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

    public int offset() { return offset; }

    public int stackPointer() { return stackPointer; }

    public int top() { return top; }
}
