package jua.runtime.heap;

public class TrueOperand extends BooleanOperand {

    public static final TrueOperand TRUE = new TrueOperand();

    private static final int HASH = (Boolean.hashCode(true) ^ Type.BOOLEAN.hashCode()) * 7;

    private TrueOperand() {
        super();
    }

    @Override
    public boolean booleanValue() {
        return true;
    }

    @Override
    public double doubleValue() {
        return 1D;
    }

    @Override
    public long longValue() {
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

    @Override
    public Operand and(Operand operand) {
        return new BooleanOperand() {
            @Override
            public boolean booleanValue() {
                return operand.booleanValue();
            }
        };
    }

    @Override
    public Operand or(Operand operand) {
        return new BooleanOperand() {
            @Override
            public boolean booleanValue() {
                return true;
            }
        };
    }

    @Override
    public Operand xor(Operand operand) {
        return new BooleanOperand() {
            @Override
            public boolean booleanValue() {
                return !operand.booleanValue();
            }
        };
    }
}
