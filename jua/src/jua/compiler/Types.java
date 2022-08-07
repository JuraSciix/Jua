package jua.compiler;

public final class Types {

    public abstract static class Type {

        public long longValue() {
            throw new UnsupportedOperationException();
        }

        public double doubleValue() {
            throw new UnsupportedOperationException();
        }

        public boolean booleanValue() {
            throw new UnsupportedOperationException();
        }

        public String stringValue() {
            throw new UnsupportedOperationException();
        }

        public boolean isLong() { return false; }
        public boolean isDouble() { return false; }
        public boolean isBoolean() { return false; }
        public boolean isString() { return false; }
        public boolean isNull() { return false; }

        public Type add(Type other) {
            throw new UnsupportedOperationException();
        }

        public Type sub(Type other) {
            throw new UnsupportedOperationException();
        }

        public Type mul(Type other) {
            throw new UnsupportedOperationException();
        }

        public Type div(Type other) {
            throw new UnsupportedOperationException();
        }

        public Type rem(Type other) {
            throw new UnsupportedOperationException();
        }

        public Type shl(Type other) {
            throw new UnsupportedOperationException();
        }

        public Type shr(Type other) {
            throw new UnsupportedOperationException();
        }

        public Type and(Type other) {
            throw new UnsupportedOperationException();
        }

        public Type or(Type other) {
            throw new UnsupportedOperationException();
        }

        public Type xor(Type other) {
            throw new UnsupportedOperationException();
        }

        public Type neg() {
            throw new UnsupportedOperationException();
        }

        public Type pos() {
            throw new UnsupportedOperationException();
        }

        public Type not() {
            throw new UnsupportedOperationException();
        }

        public Type inverse() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class LongType extends Type {

        private final long value;

        public LongType(long value) {
            this.value = value;
        }

        @Override
        public boolean isLong() {
            return true;
        }

        @Override
        public long longValue() {
            return value;
        }

        @Override
        public double doubleValue() {
            return value;
        }

        @Override
        public boolean booleanValue() {
            return (value & 1) != 0L;
        }

        @Override
        public String stringValue() {
            return Long.toString(value);
        }

        @Override
        public Type add(Type other) {
            if (other.isLong()) {
                return new LongType(value + other.longValue());
            }
            if (other.isDouble()) {
                return new DoubleType(value + other.doubleValue());
            }
            if (other.isString()) {
                return new StringType(value + other.stringValue());
            }
            return this;
        }

        @Override
        public Type sub(Type other) {
            if (other.isLong()) {
                return new LongType(value - other.longValue());
            }
            if (other.isDouble()) {
                return new DoubleType(value - other.doubleValue());
            }
            return this;
        }

        @Override
        public Type mul(Type other) {
            if (other.isLong()) {
                return new LongType(value * other.longValue());
            }
            if (other.isDouble()) {
                return new DoubleType(value * other.doubleValue());
            }
            return this;
        }

        @Override
        public Type div(Type other) {
            if (other.isLong() && other.longValue() != 0L) {
                return new LongType(value / other.longValue());
            }
            if (other.isDouble()) {
                return new DoubleType(value / other.doubleValue());
            }
            return this;
        }

        @Override
        public Type rem(Type other) {
            if (other.isLong() && other.longValue() != 0L) {
                return new LongType(value % other.longValue());
            }
            if (other.isDouble() && other.doubleValue() != 0.0D) {
                return new DoubleType(value % other.doubleValue());
            }
            return this;
        }
    }

    public static final class DoubleType extends Type {

        private final double value;

        public DoubleType(double value) {
            this.value = value;
        }

        @Override
        public boolean isDouble() {
            return true;
        }

        @Override
        public long longValue() {
            return (long) value;
        }

        @Override
        public double doubleValue() {
             return value;
        }

        @Override
        public boolean booleanValue() {
            return value != 0.0;
        }

        @Override
        public String stringValue() {
            return Double.toString(value);
        }
    }

    public static final class BooleanType extends Type {

    }

    public static final class StringType extends Type {

        private final String value;

        public StringType(String value) {
            this.value = value;
        }

        @Override
        public boolean isString() {
            return true;
        }

        @Override
        public String stringValue() {
            return value;
        }

        @Override
        public boolean booleanValue() {
            return !value.isEmpty();
        }
    }

    public static final class NullType extends Type {

        public static final NullType INSTANCE = new NullType();

        private NullType() {
            super();
        }
    }

    // todo: Задача для JavaKira
}
