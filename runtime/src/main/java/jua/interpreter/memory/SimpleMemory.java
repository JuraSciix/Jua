package jua.interpreter.memory;

import jua.interpreter.address.Address;

public class SimpleMemory implements Memory {

    private final Address[] memory;

    public SimpleMemory(Address[] memory) {
        this.memory = memory;
    }

    @Override
    public Address getAddress(int i) {
        return memory[i];
    }
}
