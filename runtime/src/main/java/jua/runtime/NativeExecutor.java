package jua.runtime;

import jua.interpreter.memory.Address;

@FunctionalInterface
public interface NativeExecutor {

    boolean execute(Address[] args, int argc, Address returnAddress);
}
