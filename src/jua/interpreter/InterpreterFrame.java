package jua.interpreter;

import jua.runtime.JuaFunction;
import jua.runtime.Operand;

public final class InterpreterFrame {

    private final InterpreterFrame callerFrame;

    private final InterpreterState state;

    private final JuaFunction ownerFunc;

    private Operand returnValue;

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

    public Operand getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Operand returnValue) {
        this.returnValue = returnValue;
    }

    void execute(InterpreterRuntime runtime) {
        state.execute(this, runtime);
    }

    public Operand getConstant(int index) {
        return ownerFunc.getProgram().getConstantPool()[index];
    }


}
