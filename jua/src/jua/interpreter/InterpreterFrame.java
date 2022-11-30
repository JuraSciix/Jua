package jua.interpreter;

import jua.runtime.JuaFunction;

public final class InterpreterFrame {

    private final InterpreterFrame callingFrame;

    private final JuaFunction owningFunction;

    private final InterpreterState state;

    // Trusting constructor.
    InterpreterFrame(InterpreterFrame callingFrame, JuaFunction owningFunction, InterpreterState state) {
        this.callingFrame = callingFrame;
        this.owningFunction = owningFunction;
        this.state = state;
    }

    public InterpreterFrame callingFrame() { return callingFrame; }
    public JuaFunction owningFunction()    { return owningFunction; }
    public InterpreterState state()  { return state; }

    public int currentLineNumber() {
        if (owningFunction().isNative()) return -1;
        return owningFunction().codeSegment().lineNumberTable().getLineNumber(state.cp());
    }
}
