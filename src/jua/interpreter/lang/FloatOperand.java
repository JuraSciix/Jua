package jua.interpreter.lang;

public class FloatOperand extends NumberOperand {

    public static final FloatOperand NaN = new FloatOperand(Double.NaN);
    public static final FloatOperand POSITIVE_INFINITY = new FloatOperand(Double.POSITIVE_INFINITY);
    public static final FloatOperand NEGATIVE_INFINITY = new FloatOperand(Double.NEGATIVE_INFINITY);

    public static FloatOperand valueOf(double value) {
        if (Double.isNaN(value)) {
            return NaN;
        }
        if (value == Double.POSITIVE_INFINITY) {
            return POSITIVE_INFINITY;
        }
        if (value == Double.NEGATIVE_INFINITY) {
            return NEGATIVE_INFINITY;
        }
        return new FloatOperand(value);
    }

    private final double value;

    public FloatOperand(double value) {
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
        if (o instanceof FloatOperand) return value == ((FloatOperand) o).value;
        return (o instanceof NumberOperand) && value == ((NumberOperand) o).floatValue();
    }

    @Override
    public int hashCode() {
        return (OperandType.FLOAT.hashCode() + 31) * 31 + Double.hashCode(value);
    }
}
