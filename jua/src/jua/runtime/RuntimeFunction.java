package jua.runtime;

import jua.interpreter.InterpreterRuntime;

// todo: НИКАКОЙ АБСТРАКЦИИ!
@Deprecated
public interface RuntimeFunction {

    void call(InterpreterRuntime env, String name, int argc);
}
