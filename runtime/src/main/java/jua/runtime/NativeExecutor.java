package jua.runtime;

import jua.interpreter.Address;

@FunctionalInterface
public interface NativeExecutor extends Function.Handle {

    boolean execute(Address[] args, int argc, Address returnAddress);
}
