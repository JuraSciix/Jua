package jua.interpreter.runtime;

import jua.interpreter.InterpreterRuntime;

public interface Function {

    void call(InterpreterRuntime env, String name, int argc);
}
