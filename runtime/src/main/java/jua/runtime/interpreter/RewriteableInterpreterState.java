package jua.runtime.interpreter;

import jua.runtime.interpreter.memory.Memory;

public class RewriteableInterpreterState implements InterpreterState {

    private int cp;

    private Memory slots;

    public void setCp(int cp) {
        this.cp = cp;
    }

    public void setSlots(Memory slots) {
        this.slots = slots;
    }

    @Override
    public int getCp() {
        return cp;
    }

    @Override
    public Memory getSlots() {
        return slots;
    }
}
