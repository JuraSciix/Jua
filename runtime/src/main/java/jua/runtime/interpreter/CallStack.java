package jua.runtime.interpreter;

import jua.runtime.Function;

public interface CallStack {

    void push(Function calleeFn, int stackBase);

    void pop();

    InterpreterFrame current();
}
