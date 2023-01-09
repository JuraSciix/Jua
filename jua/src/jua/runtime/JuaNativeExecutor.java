package jua.runtime;

import jua.interpreter.Address;

@FunctionalInterface
public interface JuaNativeExecutor {

    boolean execute(Address[] args, int argc, Address returnAddress);
}
