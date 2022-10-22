package jua.interpreter;

import jua.runtime.ValueType;
import jua.runtime.heap.Heap;
import jua.runtime.heap.MapHeap;
import jua.runtime.heap.StringHeap;
import jua.util.Conversions;

public final class Address {

    public static Address copy(Address source) {
        Address result = new Address();
        result.typeCode = source.typeCode;
        result.l = source.l;
        result.d = source.d;
        if (source.a != null) result.a = source.a.copy();
        return result;
    }

    public static Address[] allocateMemory(int offset, int count) {
        // 0 <= offset <= 2^16
        // 0 <= count <= 2^16
        // 0 <= offset+count <= 2^16

        Address[] memory = new Address[count];

        for (int i = 0; i < count; i++) {
            memory[offset + i] = new Address();
        }

        return memory;
    }

    public static void arraycopy(Address[] src, int srcOffset, Address[] dst, int dstOffset, int count) {
        // 0 <= srcOffset < src.length
        // 0 <= dstOffset < dst.length
        // 0 <= srcOffset+count <= src.length
        // 0 <= dstOffset+count <= dst.length

        for (int i = 0; i < count; i++) {
            src[srcOffset + i].quickSet(dst[dstOffset + i]);
        }
    }

    private byte typeCode;

    private long l;
    private double d;
    private Heap a;

    public byte typeCode() { return typeCode; }
    public String typeName() { return ValueType.nameOf(typeCode); }
    public boolean isScalar() { return ValueType.isTypeScalar(typeCode); }

    public long longVal() { return l; }
    public boolean booleanVal() { return Conversions.l2b(l); }
    public double doubleVal() { return d; }
    public StringHeap stringVal() { return (StringHeap) a; }
    public MapHeap mapValue() { return (MapHeap) a; }

    public void set(long _l) {
        typeCode = ValueType.LONG;
        l = _l;
    }

    public void set(boolean b) {
        typeCode = ValueType.BOOLEAN;
        l = Conversions.b2l(b);
    }

    public void set(double _d) {
        typeCode = ValueType.DOUBLE;
        d = _d;
    }

    public void set(StringHeap s) {
        typeCode = ValueType.STRING;
        a = s;
    }

    public void set(MapHeap m) {
        typeCode = ValueType.MAP;
        a = m;
    }

    public void quickSet(Address source) {
        typeCode = source.typeCode;
        l = source.l;
        d = source.d;
        a = source.a;
    }

    public void set(Address source) {
        typeCode = source.typeCode;
        l = source.l;
        d = source.d;
        if (source.a != null) a = source.a.copy();
    }

    public void slowSet(Address origin) {
        typeCode = origin.typeCode;
        l = origin.l;
        d = origin.d;
        if (origin.a != null) a = origin.a.deepCopy();
    }

    public void setNull() {
        typeCode = ValueType.NULL;
    }

    public void reset() {
        typeCode = ValueType.UNDEFINED;
        l = 0L;
        d = 0.0;
        a = null;
    }

    public boolean isSame(Address that) {
        if (that == this) return true;
        int p = ValueType.pairOf(typeCode, that.typeCode);

        if (p == ValueType.P_LL ||
            p == ValueType.P_DD) return l == that.l;
        if (p == ValueType.P_LD) return l == that.d;
        if (p == ValueType.P_DL) return d == that.l;
        if (p == ValueType.P_SS ||
            p == ValueType.P_AA ||
            p == ValueType.P_MM ||
            p == ValueType.P_II) return a.equals(that.a);
        return p == ValueType.P_NN; // null == null
    }

    @Override
    public int hashCode() {
        switch (typeCode) {
            case ValueType.UNDEFINED: throw new IllegalStateException("Trying calculate a hash-code of undefined type");
            case ValueType.LONG: return Long.hashCode(longVal());
            case ValueType.BOOLEAN: return Boolean.hashCode(booleanVal());
            case ValueType.DOUBLE: return Double.hashCode(doubleVal());
            case ValueType.STRING: // fallthrough
            case ValueType.ARRAY: // fallthrough
            case ValueType.MAP: // fallthrough
            case ValueType.ITERATOR: return a.hashCode();
            case ValueType.NULL:  return 0;
            default:  throw new IllegalStateException("Unknown type");
        }
    }

    @Override
    public boolean equals(Object o) {
        return (o == this) // Очень маловероятно
                || (o != null && o.getClass() == Address.class && isSame((Address) o));
    }

    @Override
    public String toString() {
        switch (typeCode) {
            case ValueType.UNDEFINED: return "...";
            case ValueType.LONG: return Long.toString(longVal());
            case ValueType.BOOLEAN: return Boolean.toString(booleanVal());
            case ValueType.DOUBLE: return Double.toString(doubleVal());
            case ValueType.STRING: // fallthrough
            case ValueType.ARRAY: // fallthrough
            case ValueType.MAP: // fallthrough
            case ValueType.ITERATOR:  return a.toString();
            case ValueType.NULL:  return "null";
            default:  throw new IllegalStateException("Unknown type");
        }
    }
}
