package jua.interpreter.lang;

public class FalseOperand extends BooleanOperand {

    public static final FalseOperand FALSE = new FalseOperand();

    private static final int HASH = (Boolean.hashCode(false) ^ OperandType.BOOLEAN.hashCode()) * 7;

    private FalseOperand() {
        super();
    }

    @Override
    public boolean booleanValue() {
        return false;
    }

    @Override
    public double floatValue() {
        return 0D;
    }

    @Override
    public long intValue() {
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
        return HASH;
    }
}
