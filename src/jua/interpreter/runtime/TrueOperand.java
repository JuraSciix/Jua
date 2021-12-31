package jua.interpreter.runtime;

public class TrueOperand extends BooleanOperand {

    public static final TrueOperand TRUE = new TrueOperand();

    private static final int HASH = (Boolean.hashCode(true) ^ OperandType.BOOLEAN.hashCode()) * 7;

    private TrueOperand() {
        super();
    }

    @Override
    public boolean booleanValue() {
        return true;
    }

    @Override
    public double floatValue() {
        return 1D;
    }

    @Override
    public long intValue() {
        return 1L;
    }

    @Override
    public String stringValue() {
        return "true";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TrueOperand;
    }

    @Override
    public int hashCode() {
        return HASH;
    }
}
