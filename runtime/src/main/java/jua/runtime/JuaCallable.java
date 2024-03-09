package jua.runtime;

import jua.runtime.interpreter.Address;

@FunctionalInterface
public interface JuaCallable {

    void call(Context context, Address[] args, Address returnAddress);
}
