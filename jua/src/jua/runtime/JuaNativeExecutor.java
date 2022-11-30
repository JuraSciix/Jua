package jua.runtime;

import jua.interpreter.Address;
import jua.interpreter.InterpreterThread;

public interface JuaNativeExecutor {

    boolean execute(InterpreterThread thread, Address[] args, int argc, Address returnAddress);
}
