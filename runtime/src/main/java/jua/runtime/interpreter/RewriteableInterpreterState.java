package jua.runtime.interpreter;

import jua.runtime.interpreter.memory.Memory;

public class RewriteableInterpreterState implements InterpreterState {

    private int cp;

    private int tos;

    private Memory stack;

    private Memory slots;

    public void setCp(int cp) {
        this.cp = cp;
    }

    public void setTos(int tos) {
        this.tos = tos;
    }

    public void setStack(Memory stack) {
        this.stack = stack;
    }

    public void setSlots(Memory slots) {
        this.slots = slots;
    }

    @Override
    public int getCp() {
        return cp;
    }

    @Override
    public int getTos() {
        return tos;
    }

    @Override
    public Memory getStack() {
        return stack;
    }

    @Override
    public Memory getSlots() {
        return slots;
    }
}
