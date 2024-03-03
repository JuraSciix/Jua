package jua.runtime.interpreter;

import jua.runtime.Function;

public interface InterpreterFrame {

    Function getFunction();

    InterpreterState getState();

    InterpreterFrame getCaller();

    int stackBase();
}
