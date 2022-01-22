package jua.interpreter.runtime;

import jua.interpreter.InterpreterRuntime;

// todo: НИКАКОЙ АБСТРАКЦИИ!
public interface RuntimeFunction {

    void call(InterpreterRuntime env, String name, int argc);
}
