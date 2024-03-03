package jua.runtime.interpreter;

import jua.runtime.interpreter.memory.Address;
import jua.runtime.interpreter.memory.Memory;

public interface InterpreterState {

    int getCp();

    void setCp(int cp);

    Memory getSlots();

    default void storeSlotFrom(int i, Address a) {
        getSlots().getAddress(i).set(a);
    }

    default Address getSlot(int i) {
        return getSlots().getAddress(i);
    }
}
