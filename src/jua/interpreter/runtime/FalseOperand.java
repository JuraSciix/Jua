package jua.interpreter.runtime;

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
        return 1237; // Boolean.hashCode(false);
    }
}
