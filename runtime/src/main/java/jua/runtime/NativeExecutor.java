package jua.runtime;

import jua.runtime.interpreter.memory.Address;

@FunctionalInterface
public interface NativeExecutor {

    boolean execute(Address[] args, int argc, Address returnAddress);
}
