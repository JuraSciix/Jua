package jua.interpreter;

import jua.runtime.Types;
import jua.runtime.heap.Heap;
import jua.runtime.heap.MapHeap;
import jua.runtime.heap.StringHeap;
import jua.util.Conversions;

public final class Address {

    public static Address copy(Address source) {
        Address clone = new Address();
        clone.typeCode = source.typeCode;
        clone.l = source.l;
        clone.d = source.d;
        if (source.a != null) clone.a = source.a.copy();
        return clone;
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
            src[srcOffset + i].set(dst[dstOffset + i]);
        }
    }

    private byte typeCode;

    private long l;
    private double d;
    private Heap a;

    public byte typeCode() { return typeCode; }
    public String typeName() { return Types.nameOf(typeCode); }
    public boolean isScalar() { return Types.isTypeScalar(typeCode); }

    public long longVal() { return l; }
    public boolean booleanVal() { return (l != 0L); }
    public double doubleVal() { return d; }
    public StringHeap stringVal() { return (StringHeap) a; }
    public MapHeap mapValue() { return (MapHeap) a; }

    public void set(long _l) {
        typeCode = Types.LONG;
        l = _l;
    }

    public void set(boolean b) {
        typeCode = Types.BOOLEAN;
        l = Conversions.b2l(b);
    }

    public void set(double _d) {
        typeCode = Types.DOUBLE;
        d = _d;
    }

    public void set(StringHeap s) {
        typeCode = Types.STRING;
        a = s;
    }

    public void set(MapHeap m) {
        typeCode = Types.MAP;
        a = m;
    }

    public void set(Address source) {
        typeCode = source.typeCode;
        l = source.l;
        d = source.d;
        a = source.a;
    }

    public void setNull() {
        typeCode = Types.NULL;
    }

    public void reset() {
        typeCode = Types.UNDEFINED;
        l = 0L;
        d = 0.0;
        a = null;
    }

    public boolean isSame(Address that) {
        if (that == this) return true;
        int p = Types.pairOf(typeCode, that.typeCode);

        if (p == Types.P_LL ||
            p == Types.P_DD) return l == that.l;
        if (p == Types.P_LD) return l == that.d;
        if (p == Types.P_DL) return d == that.l;
        if (p == Types.P_SS ||
            p == Types.P_AA ||
            p == Types.P_MM ||
            p == Types.P_II) return a.equals(that.a);
        return p == Types.P_NN; // null == null
    }

    @Override
    public int hashCode() {
        switch (typeCode) {
            case Types.UNDEFINED: throw new IllegalStateException("Trying calculate a hash-code of undefined value");
            case Types.LONG: return Long.hashCode(longVal());
            case Types.BOOLEAN: return Boolean.hashCode(booleanVal());
            case Types.DOUBLE: return Double.hashCode(doubleVal());
            case Types.STRING: // fallthrough
            case Types.ARRAY: // fallthrough
            case Types.MAP: // fallthrough
            case Types.ITERATOR: return a.hashCode();
            case Types.NULL:  return 0;
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
            case Types.UNDEFINED: return "...";
            case Types.LONG: return Long.toString(longVal());
            case Types.BOOLEAN: return Boolean.toString(booleanVal());
            case Types.DOUBLE: return Double.toString(doubleVal());
            case Types.STRING: // fallthrough
            case Types.ARRAY: // fallthrough
            case Types.MAP: // fallthrough
            case Types.ITERATOR:  return a.toString();
            case Types.NULL:  return "null";
            default:  throw new IllegalStateException("Unknown type");
        }
    }
}
