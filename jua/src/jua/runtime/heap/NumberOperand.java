package jua.runtime.heap;

public abstract class NumberOperand extends Operand {

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public boolean canBeBoolean() {
        return true;
    }

    @Override
    public boolean canBeFloat() {
        return true;
    }

    @Override
    public boolean canBeInt() {
        return true;
    }

    @Override
    public boolean canBeString() {
        return true;
    }
}
