package jua.runtime;

public final class NullOperand extends Operand {

    public static final NullOperand NULL = new NullOperand();

    private NullOperand() {
        super();
    }

    @Override
    public Type type() {
        return Type.NULL;
    }

    @Override
    public boolean isNull() {
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

    @Override
    public boolean booleanValue() {
        return false;
    }

    @Override
    public double doubleValue() {
        return 0D;
    }

    @Override
    public long longValue() {
        return 0L;
    }

    @Override
    public String stringValue() {
        return "null";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NullOperand;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
