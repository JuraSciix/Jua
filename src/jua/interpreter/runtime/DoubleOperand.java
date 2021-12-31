package jua.interpreter.runtime;

public class DoubleOperand extends NumberOperand {

    public static final DoubleOperand NaN = new DoubleOperand(Double.NaN);
    public static final DoubleOperand POSITIVE_INFINITY = new DoubleOperand(Double.POSITIVE_INFINITY);
    public static final DoubleOperand NEGATIVE_INFINITY = new DoubleOperand(Double.NEGATIVE_INFINITY);

    public static DoubleOperand valueOf(double value) {
        if (Double.isNaN(value)) {
            return NaN;
        }
        if (value == Double.POSITIVE_INFINITY) {
            return POSITIVE_INFINITY;
        }
        if (value == Double.NEGATIVE_INFINITY) {
            return NEGATIVE_INFINITY;
        }
        return new DoubleOperand(value);
    }

    private final double value;

    public DoubleOperand(double value) {
        this.value = value;
    }

    @Override
    public OperandType type() {
        return OperandType.FLOAT;
    }

    @Override
    public boolean booleanValue() {
        return (value != 0D);
    }

    @Override
    public boolean isFloat() {
        return true;
    }

    @Override
    public double floatValue() {
        return value;
    }

    @Override
    public long intValue() {
        return (long) value;
    }

    @Override
    public String stringValue() {
        return Double.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return !Double.isNaN(value);
        if (o instanceof DoubleOperand) return value == ((DoubleOperand) o).value;
        return (o instanceof NumberOperand) && value == ((NumberOperand) o).floatValue();
    }

    @Override
    public int hashCode() {
        return (OperandType.FLOAT.hashCode() + 31) * 31 + Double.hashCode(value);
    }

    @Override
    public Operand inc() {
        return valueOf(value + 1D);
    }

    @Override
    public Operand dec() {
        return valueOf(value - 1D);
    }
}
