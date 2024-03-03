package jua.runtime.interpreter;

import jua.runtime.interpreter.memory.Address;
import jua.runtime.Function;

public class SingleInterpreterFrame implements InterpreterFrame {

    private Function function;

    private InterpreterState state;

    private InterpreterFrame caller;


    public void setFunction(Function function) {
        this.function = function;
    }

    public void setState(InterpreterState state) {
        this.state = state;
    }

    public void setCaller(InterpreterFrame caller) {
        this.caller = caller;
    }

    @Override
    public Function getFunction() {
        return function;
    }

    @Override
    public InterpreterState getState() {
        return state;
    }

    @Override
    public InterpreterFrame getCaller() {
        return caller;
    }

    @Override
    public int regBase() {
        return 0;
    }
}
