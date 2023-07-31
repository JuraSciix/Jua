package jua.runtime;

import jua.interpreter.address.Address;

@FunctionalInterface
public interface NativeExecutor {

    boolean execute(Address[] args, int argc, Address returnAddress);
}
