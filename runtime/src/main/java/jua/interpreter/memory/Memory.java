package jua.interpreter.memory;

import jua.interpreter.address.Address;

public interface Memory {

    Address getAddress(int i);

    Memory subMemory(int offset, int count);

    int size();
}
