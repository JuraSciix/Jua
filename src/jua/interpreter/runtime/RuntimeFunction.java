package jua.interpreter.runtime;

import jua.interpreter.InterpreterRuntime;

public interface RuntimeFunction {

    void call(InterpreterRuntime env, String name, int argc);
}
