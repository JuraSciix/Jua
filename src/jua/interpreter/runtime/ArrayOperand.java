package jua.interpreter.runtime;

public class ArrayOperand extends Operand implements Cloneable {

    // todo: delegate Array class to ArrayOperand
    private Array value;

    public ArrayOperand() {
        this(new Array());
    }

    public ArrayOperand(Array value) {
        this.value = value;
    }

    @Override
    public OperandType type() {
        return OperandType.ARRAY;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean canBeArray() {
        return true;
    }

    @Override
    public boolean canBeBoolean() {
        return true;
    }

    @Override
    public boolean canBeString() {
        return true;
    }

    @Override
    public Array arrayValue() {
        return value;
    }

    @Override
    public boolean booleanValue() {
        return value.count() != 0;
    }

    @Override
    public String stringValue() {
        return value.toString0(o -> (o != this) ? o.stringValue() : "*head*");
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ArrayOperand))
            return false;
        return ((ArrayOperand) o).value.equals(value);
    }

    @Override
    public int hashCode() {
        return (OperandType.ARRAY.hashCode() ^ value.hashCode()) * 7;
    }

    @Override
    public Object clone() {
        ArrayOperand clone = (ArrayOperand) super.clone();
        clone.value = (Array) value.clone();
        return clone;
    }
}
