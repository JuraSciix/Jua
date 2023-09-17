package jua.interpreter;

import jua.interpreter.memory.Address;
import jua.runtime.Function;

public interface InterpreterFrame {

    Function getFunction();

    InterpreterState getState();

    Address getReturnAddress();

    InterpreterFrame getCaller();
}
