package jua.runtime;

import jua.interpreter.InterpreterThread;

// todo: НИКАКОЙ АБСТРАКЦИИ!
@Deprecated
public interface RuntimeFunction {

    void call(InterpreterThread env, String name, int argc);
}
