package jua.interpreter;

import jua.runtime.JuaFunction;

public final class InterpreterFrame {

    private final InterpreterFrame callerFrame;

    private final InterpreterState state;

    private final JuaFunction ownerFunc;

    public InterpreterFrame(InterpreterFrame callerFrame, InterpreterState state, JuaFunction ownerFunc) {
        this.callerFrame = callerFrame;
        this.state = state;
        this.ownerFunc = ownerFunc;
    }

    public InterpreterFrame getCallerFrame() {
        return callerFrame;
    }

    public InterpreterState getState() {
        return state;
    }

    public JuaFunction getOwnerFunc() {
        return ownerFunc;
    }

    void execute(InterpreterThread runtime) {
        state.execute(this, runtime);
    }
}
