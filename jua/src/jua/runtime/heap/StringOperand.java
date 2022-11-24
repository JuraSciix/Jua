package jua.runtime.heap;

import jua.interpreter.Address;

/**
 * @deprecated Планируется переход на {@link jua.interpreter.Address} с {@link Heap}.
 */
@Deprecated
public class StringOperand extends Operand {

    public static StringOperand valueOf(String value) {
        return new StringOperand(value);
    }

    private final String value;

    public StringOperand(String value) {
        this.value = value;
    }

    @Override
    public Type type() {
        return Type.STRING;
    }

    @Override
    public boolean isString() {
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
    public boolean booleanValue() {
        return !value.isEmpty();
    }

    @Override
    public String stringValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StringOperand)) {
            return false;
        }
        return value.equals(((StringOperand) o).value);
    }

    @Override
    public int hashCode() {
        return (Type.STRING.hashCode() ^ value.hashCode()) * 7;
    }

    @Override
    public int length() {
        return value.length();
    }

    @Override
    public void writeToAddress(Address address) {
        address.set(new StringHeap(value));
    }

    @Override
    public Operand add(Operand operand) {
        return new StringOperand(value.concat(operand.stringValue()));
    }
}
