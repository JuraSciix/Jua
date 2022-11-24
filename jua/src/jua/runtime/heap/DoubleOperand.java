package jua.runtime.heap;

import jua.interpreter.Address;
import jua.interpreter.InterpreterError;

/**
 * @deprecated Планируется переход на {@link jua.interpreter.Address} с {@link Heap}.
 */
@Deprecated
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
    public Type type() { return Type.DOUBLE; }

    @Override
    public boolean booleanValue() { return (value != 0D); }

    @Override
    public boolean isDouble() { return true; }

    @Override
    public double doubleValue() { return value; }

    @Override
    public long longValue() { return (long) value; }

    @Override
    public String stringValue() { return Double.toString(value); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return !Double.isNaN(value);
        if (o instanceof DoubleOperand) return value == ((DoubleOperand) o).value;
        return (o instanceof NumberOperand) && value == ((NumberOperand) o).doubleValue();
    }

    @Override
    public int hashCode() { return Double.hashCode(value); }

    @Override
    public Operand add(Operand operand) {
        return new DoubleOperand(value + operand.doubleValue());
    }

    @Override
    public Operand mul(Operand operand) {
        return new DoubleOperand(value * operand.doubleValue());
    }

    @Override
    public Operand div(Operand operand) {
        return new DoubleOperand(value / operand.doubleValue());
    }


    @Override
    public Operand sub(Operand operand) {
        return new DoubleOperand(value - operand.doubleValue());
    }

    @Override
    public Operand rem(Operand operand) {
        double r = operand.doubleValue();
        if (r == 0D) {
            throw InterpreterError.divisionByZero();
        }

        return new DoubleOperand(value % r);
    }

    @Override
    public Operand neg() {
        return new DoubleOperand(-value);
    }
    @Override
    public Operand increment() { return valueOf(value + 1D); }

    @Override
    public Operand decrement() { return valueOf(value - 1D); }

    @Override
    public void writeToAddress(Address address) {
        address.set(value);
    }
}
