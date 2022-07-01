package jua.runtime.heap;

public final class FalseOperand extends BooleanOperand {

    public static final FalseOperand FALSE = new FalseOperand();

    private FalseOperand() {
        super();
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
        return "false";
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof FalseOperand);
    }

    @Override
    public int hashCode() {
        return 1237; // Boolean.hashCode(false);
    }

    @Override
    public Operand and(Operand operand) {
        return this;
    }

    @Override
    public Operand or(Operand operand) {
        return operand;
    }

    @Override
    public Operand xor(Operand operand) {
        return this;
    }
}
