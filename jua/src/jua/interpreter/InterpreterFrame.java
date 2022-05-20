package jua.interpreter;

import jua.runtime.JuaFunction;

public final class InterpreterFrame {

    private final InterpreterFrame sender;

    private final JuaFunction function;

    private final InterpreterState state;

    // Trusting constructor.
    InterpreterFrame(InterpreterFrame sender, JuaFunction function, InterpreterState state) {
        this.sender = sender;
        this.function = function;
        this.state = state;
    }

    public InterpreterFrame sender() { return sender; }
    public JuaFunction function()    { return function; }
    public InterpreterState state()  { return state; }
}
