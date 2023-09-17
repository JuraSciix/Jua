package jua.interpreter;

import jua.interpreter.memory.Address;
import jua.runtime.Function;

public interface CallStack {

    void push(Function calleeFn, Address returnAddr);

    void pop();

    InterpreterFrame current();
}
