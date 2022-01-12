package jua.interpreter.runtime;

public class StringOperand extends Operand {

    public static StringOperand valueOf(String value) {
        return new StringOperand(value);
    }

    private final String value;

    public StringOperand(String value) {
        this.value = value;
    }

    @Override
    public OperandType type() {
        return OperandType.STRING;
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
        return (OperandType.STRING.hashCode() ^ value.hashCode()) * 7;
    }
}
