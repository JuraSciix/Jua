package jua.runtime.interpreter;

import jua.runtime.interpreter.memory.Address;
import jua.runtime.Function;

public interface InterpreterFrame {

    Function getFunction();

    InterpreterState getState();

    Address getReturnAddress();

    InterpreterFrame getCaller();
}
