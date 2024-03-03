package jua.runtime.interpreter;

import jua.runtime.Function;

public interface CallStack {

    void push(Function calleeFn);

    void pop();

    InterpreterFrame current();
}
