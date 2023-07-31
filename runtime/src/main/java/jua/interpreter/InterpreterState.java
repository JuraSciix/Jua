package jua.interpreter;

import jua.interpreter.address.Address;
import jua.interpreter.memory.Memory;

public class InterpreterState {

    private final Memory stack;

    private final Memory slots;

    private int cp = 0; // Code Pointer

    private int tos = 0; // Top Of Stack

    public InterpreterState(Memory stack, Memory slots) {
        this.stack = stack;
        this.slots = slots;
    }

    public int getCp() {
        return cp;
    }

    public void setCp(int cp) {
        this.cp = cp;
    }

    public int getTos() {
        return tos;
    }

    public void setTos(int tos) {
        this.tos = tos;
    }

    public Memory getStack() {
        return stack;
    }

    public Memory getSlots() {
        return slots;
    }

    public void addTos(int tos) {
        this.tos += tos;
    }

    public void loadStackTo(Address a) {
        a.set(getStack().getAddress(getTos()));
    }

    public void storeStackFrom(Address a) {
        getStack().getAddress(getTos()).set(a);
    }

    /**
     * Возвращает регистр стека.
     * Если {@code i < 0}, то возвращается записанный регистр, иначе свободный.
     */
    public Address getStackAddress(int i) {
        return getStack().getAddress(getTos() + i);
    }

    public void loadSlotTo(int i, Address a) {
        a.set(getStack().getAddress(i));
    }

    public void storeSlotFrom(int i, Address a) {
        getSlots().getAddress(i).set(a);
    }

    public Address getSlot(int i) {
        return getSlots().getAddress(i);
    }
}
