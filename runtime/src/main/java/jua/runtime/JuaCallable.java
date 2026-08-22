package jua.runtime;

import jua.vm.Address;

@FunctionalInterface
public interface JuaCallable {

    void call(Context context, Address[] args, Address returnAddress);
}
