package jua.interpreter.runtime;

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
    public Operand increment() { return valueOf(value + 1L); }

    @Override
    public Operand decrement() { return valueOf(value - 1L); }
}
