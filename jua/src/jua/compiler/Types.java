package jua.compiler;

import jua.runtime.heap.*;
import jua.util.Conversions;

import java.util.Objects;

public interface Types {

    Type TYPE_NULL = new NullType();
    Type TYPE_TRUE = new BooleanType(true);
    Type TYPE_FALSE = new BooleanType(false);

    static Type nullType() {
        return TYPE_NULL;
    }

    static Type ofBoolean(boolean value) {
        return value ? TYPE_TRUE : TYPE_FALSE;
    }

    abstract class Type implements Comparable<Type> {

        public boolean isNull() { return this instanceof NullType; }
        public boolean isNumber() { return this instanceof NumberType; }
        public boolean isScalar() { return this instanceof ScalarType; }
        public boolean isLong() { return this instanceof LongType; }
        public boolean isDouble() { return this instanceof DoubleType; }
        public boolean isBoolean() { return this instanceof BooleanType; }
        public boolean isString() { return this instanceof StringType; }

        public long longValue() { throw exception(); }
        public double doubleValue() { throw exception(); }
        public boolean booleanValue() { throw exception(); }
        public String stringValue() { throw exception(); }

        private UnsupportedOperationException exception() {
            return new UnsupportedOperationException(getClass().getName());
        }

        @Deprecated
        public abstract Operand toOperand();

        @Deprecated
        public int quickCompare(Type other, int except) {
            try {
                return compareTo(other);
            } catch (UnsupportedOperationException e) {
                return except;
            }
        }

        @Deprecated
        public Type copy(Types types) { return this; }

        public abstract int resolvePoolConstant(Code code);
    }

    class NullType extends Type {

        @Override
        public Operand toOperand() { return NullOperand.NULL; }

        @Override
        public int resolvePoolConstant(Code code) {
            return code.constantPoolWriter().writeNull();
        }

        @Override
        public int compareTo(Type o) {
            return (this == o) ? 0 : -1; /* null always < x */
        }

        @Override
        public int hashCode() { return 0; }

        @Override
        public boolean equals(Object o) { return this == o; }

        @Override
        public String toString() { return "null"; }
    }

    abstract class ScalarType extends Type {

    }

    abstract class NumberType extends ScalarType {

        @Override
        public boolean isNumber() {
            return true;
        }
    }

    class LongType extends NumberType {

        final long value;

        LongType(long value) {
            this.value = value;
        }

        @Override
        public long longValue() { return value; }

        @Override
        public double doubleValue() { return (double) value; }

        @Override
        public boolean booleanValue() { return Conversions.l2b(value); }

        @Override
        public String stringValue() { return Long.toString(value); }

        @Override
        public Operand toOperand() { return LongOperand.valueOf(value); }

        @Override
        public int resolvePoolConstant(Code code) {
            return code.constantPoolWriter().writeLong(value);
        }

        @Override
        public int compareTo(Type o) {
            return Long.compare(value, o.longValue());
        }

        @Override
        public int hashCode() { return Long.hashCode(value); }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LongType t = (LongType) o;
            return value == t.value;
        }

        @Override
        public String toString() { return Long.toString(value); }
    }

    class DoubleType extends NumberType {

        final double value;

        DoubleType(double value) {
            this.value = value;
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
        public Operand toOperand() { return DoubleOperand.valueOf(value); }

        @Override
        public int resolvePoolConstant(Code code) {
            return code.constantPoolWriter().writeDouble(value);
        }

        @Override
        public int compareTo(Type o) {
            return Double.compare(value, o.doubleValue());
        }

        @Override
        public int hashCode() { return Double.hashCode(value); }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DoubleType t = (DoubleType) o;
            return Double.compare(t.value, value) == 0;
        }

        @Override
        public String toString() { return Double.toString(value); }
    }

    class BooleanType extends ScalarType {

        final boolean value;

        BooleanType(boolean value) {
            this.value = value;
        }

        @Override
        public long longValue() { return Conversions.b2l(value); }

        @Override
        public double doubleValue() { return Conversions.b2d(value); }

        @Override
        public boolean booleanValue() { return value; }

        @Override
        public String stringValue() { return Boolean.toString(value); }

        @Override
        public Operand toOperand() { return BooleanOperand.valueOf(value); }

        @Override
        public int resolvePoolConstant(Code code) {
            return value ? code.constantPoolWriter().writeTrue() : code.constantPoolWriter().writeFalse();
        }

        @Override
        public int compareTo(Type o) {
            return Boolean.compare(value, o.booleanValue());
        }

        @Override
        public int hashCode() { return Boolean.hashCode(value); }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BooleanType t = (BooleanType) o;
            return value == t.value;
        }

        @Override
        public String toString() { return Boolean.toString(value); }
    }

    class StringType extends ScalarType {

        final String value;

        StringType(String value) {
            this.value = Objects.requireNonNull(value, "Value is null");
        }

        @Override
        public boolean booleanValue() { return !value.isEmpty(); }

        @Override
        public String stringValue() { return value; }

        @Override
        public Operand toOperand() { return new StringOperand(value); }

        @Override
        public int resolvePoolConstant(Code code) {
            return code.constantPoolWriter().writeString(value);
        }

        @Override
        public int compareTo(Type o) {
            return value.compareTo(o.stringValue());
        }

        @Override
        public int hashCode() { return value.hashCode(); }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StringType t = (StringType) o;
            return Objects.equals(value, t.value);
        }

        @Override
        public String toString() { return "\"" + value + "\""; }
    }
}
