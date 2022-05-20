package jua.runtime.heap;

public abstract class BooleanOperand extends Operand {

    public static BooleanOperand valueOf(long value) {
        return valueOf(value != 0);
    }

    public static BooleanOperand valueOf(boolean value) {
        return (value ? TrueOperand.TRUE : FalseOperand.FALSE);
    }

    @Override
    public Type type() {
        return Type.BOOLEAN;
    }

    @Override
    public boolean isBoolean() {
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
