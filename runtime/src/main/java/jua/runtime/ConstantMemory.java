package jua.runtime;

import jua.interpreter.address.Address;

public final class ConstantMemory {

    public final String name;

    public final Address address;

    public ConstantMemory(String name, Address address) {
        this.name = name;
        this.address = address;
    }
}
