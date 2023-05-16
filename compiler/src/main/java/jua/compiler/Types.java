package jua.compiler;

import jua.interpreter.Address;
import jua.runtime.heap.*;
import jua.utils.Assert;

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
        public boolean isPrimitive() { return this instanceof PrimitiveType; }
        public boolean isScalar() { return this instanceof ScalarType; }
        public boolean isLong() { return this instanceof LongType; }
        public boolean isDouble() { return this instanceof DoubleType; }
        public boolean isBoolean() { return this instanceof BooleanType; }
        public boolean isString() { return this instanceof StringType; }
        public boolean isList() { return this instanceof ListType; }
        public boolean isMap() { return this instanceof MapType; }

        public long longValue() { throw exception(); }
        public double doubleValue() { throw exception(); }
        public boolean booleanValue() { throw exception(); }
        public String stringValue() { throw exception(); }

        private UnsupportedOperationException exception() {
            return new UnsupportedOperationException(getClass().getName());
        }

        public abstract void write2address(Address address);

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

    class NullType extends PrimitiveType {

        @Override
        public void write2address(Address address) {
            address.setNull();
        }

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

    abstract class PrimitiveType extends Type {

    }

    abstract class ScalarType extends PrimitiveType {

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
        public boolean booleanValue() {
            return jua.runtime.Types.l2b(value);
        }

        @Override
        public String stringValue() { return Long.toString(value); }

        @Override
        public void write2address(Address address) {
            address.set(value);
        }

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
        public boolean booleanValue() {
            return jua.runtime.Types.d2b(value);
        }

        @Override
        public String stringValue() { return Double.toString(value); }

        @Override
        public void write2address(Address address) {
            address.set(value);
        }

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
        public long longValue() {
            return jua.runtime.Types.b2l(value);
        }

        @Override
        public double doubleValue() {
            return jua.runtime.Types.b2d(value);
        }

        @Override
        public boolean booleanValue() { return value; }

        @Override
        public String stringValue() { return Boolean.toString(value); }

        @Override
        public void write2address(Address address) {
            address.set(value);
        }

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
        public void write2address(Address address) {
            address.set(new StringHeap(value));
        }

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

    class ListType extends Type {

        final Type[] elements;

        ListType(Type[] elements) {
            this.elements = elements;
        }

        @Override
        public boolean booleanValue() { return elements.length > 0; }

        @Override
        public void write2address(Address address) {
            ListHeap h = new ListHeap(elements.length);
            for (int i = 0; i < elements.length; i++) {
                elements[i].write2address(h.get(i));
            }
            address.set(h);
        }

        @Override
        public int resolvePoolConstant(Code code) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int compareTo(Type o) {
            throw new UnsupportedOperationException();
        }
    }

    class MapType extends Type {

        final Type[] keys;
        final Type[] values;

        MapType(Type[] keys, Type[] values) {
            Assert.ensure(keys.length == values.length);
            this.keys = keys;
            this.values = values;
        }

        @Override
        public boolean booleanValue() {
            return keys.length > 0;
        }

        @Override
        public void write2address(Address address) {
            MapHeap h = new MapHeap();
            for (int i = 0; i < keys.length; i++) {
                Address key   = new Address();
                Address value = new Address();
                keys[i].write2address(key);
                values[i].write2address(value);
                h.put(key, value);
            }
            address.set(h);
        }

        @Override
        public int resolvePoolConstant(Code code) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int compareTo(Type o) {
            throw new UnsupportedOperationException();
        }
    }
}
