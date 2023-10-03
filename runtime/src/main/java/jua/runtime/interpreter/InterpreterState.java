package jua.runtime.interpreter;

import jua.runtime.interpreter.memory.Address;
import jua.runtime.interpreter.memory.Memory;

public interface InterpreterState {

    int getCp();

    void setCp(int cp);

    int getTos();

    void setTos(int tos);

    Memory getStack();

    Memory getSlots();

    default void addTos(int tos) {
        setTos(getTos() + tos);
    }

    default void loadStackTo(Address a) {
        a.set(getStack().getAddress(getTos()));
    }

    default void storeStackFrom(Address a) {
        getStack().getAddress(getTos()).set(a);
    }

    /**
     * Возвращает регистр стека.
     * Если {@code i < 0}, то возвращается записанный регистр, иначе свободный.
     */
    default Address getStackAddress(int i) {
        return getStack().getAddress(getTos() + i);
    }

    default void loadSlotTo(int i, Address a) {
        a.set(getStack().getAddress(i));
    }

    default void storeSlotFrom(int i, Address a) {
        getSlots().getAddress(i).set(a);
    }

    default Address getSlot(int i) {
        return getSlots().getAddress(i);
    }
}
