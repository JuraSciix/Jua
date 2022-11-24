package jua.runtime.heap;

import jua.interpreter.Address;
import jua.interpreter.InterpreterError;

/**
 * @deprecated Планируется переход на {@link jua.interpreter.Address} с {@link Heap}.
 */
@Deprecated
public final class LongOperand extends NumberOperand {

    private static final LongOperand[] POOL;

    static {
        // todo: Размер пула должен быть регулируемым
        LongOperand[] pool = new LongOperand[256];

        for (int i = 0; i < 256; i++) {
            pool[i] = new LongOperand(i - 128);
        }
        POOL = pool;
    }

    public static LongOperand valueOf(long value) {
        return ((value + 128) >>> 8 == 0L) ? POOL[(int) (value + 128)] : new LongOperand(value);
    }

    private final long value;

    public LongOperand(long value) { this.value = value; }

    @Override
    public Type type() { return Type.LONG; }

    @Override
    public boolean isLong() { return true; }

    @Override
    public boolean booleanValue() { return (value != 0L); }

    @Override
    public double doubleValue() { return (double) value; }

    @Override
    public long longValue() { return value; }

    @Override
    public String stringValue() { return Long.toString(value); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof LongOperand) return value == ((LongOperand) o).value;
        return (o instanceof NumberOperand) && value == ((NumberOperand) o).doubleValue();
    }

    @Override
    public int hashCode() { return Long.hashCode(value); }

    @Override
    public Operand add(Operand operand) {
        return new LongOperand(value + operand.longValue());
    }

    @Override
    public Operand and(Operand operand) {
        return new LongOperand(value & operand.longValue());
    }

    @Override
    public Operand or(Operand operand) {
        return new LongOperand(value | operand.longValue());
    }

    @Override
    public Operand xor(Operand operand) {
        return new LongOperand(value ^ operand.longValue());
    }

    @Override
    public Operand shl(Operand operand) {
        return new LongOperand(value << operand.longValue());
    }

    @Override
    public Operand shr(Operand operand) {
        return new LongOperand(value >> operand.longValue());
    }

    @Override
    public Operand sub(Operand operand) {
        return new LongOperand(value - operand.longValue());
    }

    @Override
    public Operand mul(Operand operand) {
        return new LongOperand(value * operand.longValue());
    }

    @Override
    public Operand div(Operand operand) {
        long l = value;
        long r = operand.longValue();

        if (r == 0) {
            throw InterpreterError.divisionByZero();
        }
        if ((l % r) == 0) {
            return new LongOperand(l / r);
        } else {
            return new DoubleOperand((double) l / r);
        }
    }

    @Override
    public Operand rem(Operand operand) {
        long r = operand.longValue();
        if (r == 0L) {
            throw InterpreterError.divisionByZero();
        }

        return new LongOperand(value % r);
    }

    @Override
    public Operand not() {
        return new LongOperand(~value);
    }

    @Override
    public Operand neg() {
        return new LongOperand(-value);
    }

    @Override
    public Operand increment() { return valueOf(value + 1L); }

    @Override
    public Operand decrement() { return valueOf(value - 1L); }

    @Override
    public void writeToAddress(Address address) {
        address.set(value);
    }
}
