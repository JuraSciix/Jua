package jua.interpreter.runtime;

public final class DoubleOperand extends NumberOperand {

    public static final DoubleOperand NaN = new DoubleOperand(Double.NaN);

    public static final DoubleOperand POS_INF = new DoubleOperand(Double.POSITIVE_INFINITY);

    public static final DoubleOperand NEG_INF = new DoubleOperand(Double.NEGATIVE_INFINITY);

    public static DoubleOperand valueOf(double value) {
        return Double.isFinite(value) ? new DoubleOperand(value) :
               Double.isNaN(value) ? NaN :
               (value >= 0.0D) ? POS_INF : NEG_INF;
    }

    private final double value;

    public DoubleOperand(double value) { this.value = value; }

    @Override
    public OperandType type() { return OperandType.FLOAT; }

    @Override
    public boolean booleanValue() { return (value != 0D); }

    @Override
    public boolean isFloat() { return true; }

    @Override
    public double floatValue() { return value; }

    @Override
    public long intValue() { return (long) value; }

    @Override
    public String stringValue() { return Double.toString(value); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return !Double.isNaN(value);
        if (o instanceof DoubleOperand) return value == ((DoubleOperand) o).value;
        return (o instanceof NumberOperand) && value == ((NumberOperand) o).floatValue();
    }

    @Override
    public int hashCode() { return Double.hashCode(value); }

    @Override
    public Operand inc() { return valueOf(value + 1D); }

    @Override
    public Operand dec() { return valueOf(value - 1D); }
}
