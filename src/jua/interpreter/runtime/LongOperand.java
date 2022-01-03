package jua.interpreter.runtime;

public class LongOperand extends NumberOperand {

    private static final LongOperand[] CACHE = new LongOperand[256];

    static {
        for (int i = 0; i < 256; i++) {
            CACHE[i] = new LongOperand(i - 128);
        }
    }

    public static LongOperand valueOf(long value) {
        return (value >= -128 && value <= 127) ? CACHE[(int) value + 128] : new LongOperand(value);
    }

    private final long value;

    public LongOperand(long value) {
        this.value = value;
    }

    @Override
    public OperandType type() {
        return OperandType.INT;
    }

    @Override
    public boolean isInt() {
        return true;
    }

    @Override
    public boolean booleanValue() {
        return (value != 0L);
    }

    @Override
    public double floatValue() {
        return (double) value;
    }

    @Override
    public long intValue() {
        return value;
    }

    @Override
    public String stringValue() {
        return Long.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof LongOperand) return value == ((LongOperand) o).value;
        return (o instanceof NumberOperand) && value == ((NumberOperand) o).floatValue();
    }

    @Override
    public int hashCode() {
        return (OperandType.INT.hashCode() + 31) * 31 + Long.hashCode(value);
    }

    @Override
    public Operand inc() {
        return valueOf(value + 1L);
    }

    @Override
    public Operand dec() {
        return valueOf(value - 1L);
    }
}