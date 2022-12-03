package jua.compiler;

import jua.runtime.heap.*;
import jua.util.Conversions;

import java.util.Objects;

public final class Types {

    private final Code code;

    public Types(Code code) {
        this.code = Objects.requireNonNull(code, "Code is null");
    }

    public static abstract class Type {

        public boolean isLong() { return false; }

        public boolean isDouble() { return false; }

        public boolean isBoolean() { return false; }

        public boolean isString() { return false; }

        public boolean isNull() { return false; }

        public boolean isNumber() { return false; }

        public boolean isScalar() { return false; }

        public long longValue() { throw new UnsupportedOperationException(); }

        public double doubleValue() { throw new UnsupportedOperationException(); }

        public boolean booleanValue() { throw new UnsupportedOperationException(); }

        public String stringValue() { throw new UnsupportedOperationException(); }

        public abstract int getConstantIndex();

        public abstract Operand toOperand();

        public abstract int quickCompare(Type other, int except);

        public abstract Type copy(Types types);
    }

    public static abstract class ScalarType extends Type {

        @Override
        public boolean isScalar() { return true; }
    }

    public static abstract class NumberType extends ScalarType {

        @Override
        public boolean isNumber() { return true; }
    }

    public final class LongType extends NumberType {

        private final long value;

        public LongType(long value) {
            this.value = value;
        }

        @Override
        public boolean isLong() { return true; }

        @Override
        public long longValue() { return value; }

        @Override
        public double doubleValue() { return value; }

        @Override
        public boolean booleanValue() { return Conversions.l2b(value); }

        @Override
        public String stringValue() { return Long.toString(value); }

        @Override
        public int getConstantIndex() { return code.resolveLong(value); }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LongType longType = (LongType) o;
            return value == longType.value;
        }

        @Override
        public Operand toOperand() { return LongOperand.valueOf(value); }

        @Override
        public int quickCompare(Type other, int except) {
            if (other.isLong()) return Long.compare(value, other.longValue());
            if (other.isDouble()) return Double.compare(value, other.doubleValue());
            return except;
        }

        @Override
        public Type copy(Types types) { return types.asLong(value); }
    }

    public final class DoubleType extends NumberType {

        private final double value;

        public DoubleType(double value) {
            this.value = value;
        }

        @Override
        public boolean isDouble() {
            return true;
        }

        @Override
        public long longValue() { return (long) value; }

        @Override
        public double doubleValue() { return value; }

        @Override
        public boolean booleanValue() { return Conversions.d2b(value); }

        @Override
        public String stringValue() { return Double.toString(value); }

        @Override
        public int getConstantIndex() { return code.resolveDouble(value); }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DoubleType that = (DoubleType) o;
            return Double.compare(that.value, value) == 0;
        }

        @Override
        public Operand toOperand() { return DoubleOperand.valueOf(value); }

        @Override
        public int quickCompare(Type other, int except) {
            if (other.isLong())  return Double.isNaN(value) ? except : Double.compare(value, other.longValue());
            if (other.isDouble()) return Double.isNaN(value) || Double.isNaN(other.doubleValue()) ? except : Double.compare(value, other.doubleValue());
            return except;
        }

        @Override
        public Type copy(Types types) { return types.asDouble(value); }
    }

    public static abstract class BooleanType extends ScalarType {

        @Override
        public boolean isBoolean() { return true; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BooleanType that = (BooleanType) o;
            return Objects.equals(booleanValue(), that.booleanValue());
        }

        @Override
        public Type copy(Types types) { return types.asBoolean(booleanValue()); }
    }

    public final class TrueType extends BooleanType {

        private TrueType() {
            super();
        }

        @Override
        public long longValue() { return 1L; }

        @Override
        public double doubleValue() { return 1.0; }

        @Override
        public boolean booleanValue() { return true; }

        @Override
        public String stringValue() { return "true"; }

        @Override
        public int getConstantIndex() { return code.get_cpb().putTrueEntry(); }

        @Override
        public Operand toOperand() { return TrueOperand.TRUE; }

        @Override
        public int quickCompare(Type other, int except) {
            return other.isBoolean() && other.booleanValue() ? 0 : except;
        }
    }

    public final class FalseType extends BooleanType {

        private FalseType() {
            super();
        }

        @Override
        public long longValue() { return 0L; }

        @Override
        public double doubleValue() { return 0.0; }

        @Override
        public boolean booleanValue() { return false; }

        @Override
        public String stringValue() { return "false"; }

        @Override
        public int getConstantIndex() { return code.get_cpb().putFalseEntry(); }

        @Override
        public Operand toOperand() { return FalseOperand.FALSE; }

        @Override
        public int quickCompare(Type other, int except) {
            return other.isBoolean() && !other.booleanValue() ? 0 : except;
        }
    }

    public final class StringType extends ScalarType {

        private final String value;

        public StringType(String value) {
            this.value = Objects.requireNonNull(value, "Value is null");
        }

        @Override
        public boolean isString() { return true; }

        @Override
        public boolean booleanValue() { return !value.isEmpty(); }

        @Override
        public String stringValue() { return value; }

        @Override
        public int getConstantIndex() { return code.resolveString(value); }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StringType that = (StringType) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public Operand toOperand() { return new StringOperand(value); }

        @Override
        public int quickCompare(Type other, int except) {
            return other.isString() && value.length() == other.stringValue().length() ? value.compareTo(other.stringValue()) : except;
        }

        @Override
        public Type copy(Types types) { return types.asString(value); }
    }

    public final class NullType extends Type {

        private NullType() {
            super();
        }

        @Override
        public boolean isNull() { return true; }

        @Override
        public long longValue() { return 0L; }

        @Override
        public double doubleValue() { return 0.0; }

        @Override
        public boolean booleanValue() { return false; }

        @Override
        public String stringValue() { return "null"; }

        @Override
        public int getConstantIndex() { return code.get_cpb().putNullEntry(); }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return true;
        }

        @Override
        public Operand toOperand() { return NullOperand.NULL; }

        @Override
        public int quickCompare(Type other, int except) {
            return other.isNull() ? 0 : except;
        }

        @Override
        public Type copy(Types types) { return types.asNull(); }
    }

    public final TrueType True = new TrueType();

    public final FalseType False = new FalseType();

    public final NullType Null = new NullType();

    public LongType asLong(long value) { return new LongType(value); }

    public DoubleType asDouble(double value) { return new DoubleType(value); }

    public BooleanType asBoolean(boolean value) { return value ? True : False; }

    public StringType asString(String value) { return new StringType(value); }

    public NullType asNull() { return Null; }
}
