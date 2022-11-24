package jua.interpreter;

import jua.runtime.ValueType;
import jua.runtime.heap.Heap;
import jua.runtime.heap.MapHeap;
import jua.runtime.heap.StringHeap;
import jua.util.Conversions;

import static jua.interpreter.InterpreterThread.currentThread;
import static jua.runtime.ValueType.*;

public final class Address {

    public static Address copy(Address source) {
        Address copy = new Address();
        copy.set(source);
        return copy;
    }

    public static Address[] allocateMemory(int offset, int count) {
        // 0 <= offset <= 2^16
        // 0 <= count <= 2^16
        // 0 <= offset+count <= 2^16

        Address[] memory = new Address[count];

        for (int i = offset; i < count; i++) {
            memory[i] = new Address();
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

    private byte type;

    private long l;
    private double d;
    private Heap a;

    public byte typeCode() {
        return type;
    }

    public String typeName() {
        return ValueType.nameOf(type);
    }

    public boolean isScalar() {
        return ValueType.isTypeScalar(type);
    }

    public long longVal() {
        return l;
    }

    public boolean booleanVal() {
        return Conversions.l2b(l);
    }

    public double doubleVal() {
        return d;
    }

    public StringHeap stringVal() {
        return (StringHeap) a;
    }

    public MapHeap mapValue() {
        return (MapHeap) a;
    }

    public boolean testType(byte type) {
        if (type == this.type) {
            return true;
        }
        currentThread().error(nameOf(type) + " expected, " + typeName()+ " got");
        return false;
    }

    public boolean isNull() {
        return type == NULL;
    }

    public void set(long _l) {
        type = ValueType.LONG;
        l = _l;
    }

    public void set(boolean b) {
        type = ValueType.BOOLEAN;
        l = Conversions.b2l(b);
    }

    public void set(double _d) {
        type = ValueType.DOUBLE;
        d = _d;
    }

    public void set(StringHeap s) {
        type = ValueType.STRING;
        a = s;
    }

    public void set(MapHeap m) {
        type = ValueType.MAP;
        a = m;
    }

    public void quickSet(Address source) {
        type = source.type;
        l = source.l;
        d = source.d;
        a = source.a;
    }

    public void set(Address source) {
        switch (source.type) {
            case UNDEFINED:
                throw new IllegalArgumentException();
            case LONG:
                type = LONG;
                l = source.l;
                break;
            case DOUBLE:
                type = DOUBLE;
                d = source.d;
                break;
            case BOOLEAN:
                type = BOOLEAN;
                l = source.l;
                break;
            case STRING:
                type = STRING;
                a = source.stringVal().copy();
            case ARRAY:
                // todo
                break;
            case MAP:
                type = MAP;
                a = source.mapValue().copy();
                break;
            case ITERATOR:
                // todo
                break;
            case NULL:
                type = NULL;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void slowSet(Address source) {
        switch (source.type) {
            case UNDEFINED:
                throw new IllegalArgumentException();
            case LONG:
                type = LONG;
                l = source.l;
                break;
            case DOUBLE:
                type = DOUBLE;
                d = source.d;
                break;
            case BOOLEAN:
                type = BOOLEAN;
                l = source.l;
                break;
            case STRING:
                type = STRING;
                a = source.stringVal().deepCopy();
            case ARRAY:
                // todo
                break;
            case MAP:
                type = MAP;
                a = source.mapValue().deepCopy();
                break;
            case ITERATOR:
                // todo
                break;
            case NULL:
                type = NULL;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void setNull() {
        type = ValueType.NULL;
    }

    public void reset() {
        type = ValueType.UNDEFINED;
        l = 0L;
        d = 0.0;
        a = null;
    }

    public void add(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL) {
            result.set(l + rhs.l);
            return;
        }
        if (union == P_LD) {
            result.set(l + rhs.d);
            return;
        }
        if (union == P_DL) {
            result.set(l + rhs.l);
            return;
        }
        if (union == P_DD) {
            result.set(l + rhs.d);
            return;
        }
        if (union == P_LS) {
            result.set(new StringHeap().append(longVal()).append(rhs.stringVal()));
            return;
        }
        if (union == P_DS) {
            result.set(new StringHeap().append(doubleVal()).append(rhs.stringVal()));
            return;
        }
        if (union == P_BS) {
            result.set(new StringHeap().append(booleanVal()).append(rhs.stringVal()));
            return;
        }
        if (union == P_SS) {
            if (this == result) {
                stringVal().append(rhs.stringVal());
            } else {
                rhs.set(new StringHeap().append(stringVal()).append(rhs.stringVal()));
            }
            return;
        }
        if (union == P_NS) {
            result.set(new StringHeap().appendNull().append(rhs.stringVal()));
            return;
        }
        if (union == P_SL) {
            if (this == result) {
                stringVal().append(rhs.longVal());
            } else {
                rhs.set(new StringHeap(stringVal()).append(rhs.longVal()));
            }
            return;
        }
        if (union == P_SD) {
            if (this == result) {
                stringVal().append(rhs.doubleVal());
            } else {
                rhs.set(new StringHeap(stringVal()).append(rhs.doubleVal()));
            }
            return;
        }
        if (union == P_SB) {
            if (this == result) {
                stringVal().append(rhs.booleanVal());
            } else {
                rhs.set(new StringHeap(stringVal()).append(rhs.booleanVal()));
            }
            return;
        }
        if (union == P_SN) {
            if (this == result) {
                stringVal().appendNull();
            } else {
                rhs.set(new StringHeap(stringVal()).appendNull());
            }
            return;
        }
        binaryOperatorError("*", rhs);
    }

    public void sub(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL) {
            result.set(l - rhs.l);
            return;
        }
        if (union == P_LD) {
            result.set(l - rhs.d);
            return;
        }
        if (union == P_DL) {
            result.set(l - rhs.l);
            return;
        }
        if (union == P_DD) {
            result.set(l - rhs.d);
            return;
        }
        // todo: arrays, maps
        binaryOperatorError("-", rhs);
    }

    public void mul(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL) {
            result.set(l * rhs.l);
            return;
        }
        if (union == P_LD) {
            result.set(l * rhs.d);
            return;
        }
        if (union == P_DL) {
            result.set(l * rhs.l);
            return;
        }
        if (union == P_DD) {
            result.set(l * rhs.d);
            return;
        }
        binaryOperatorError("*", rhs);
    }

    public void div(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL) {
            if (rhs.l == 0L) {
                currentThread().error("division by zero");
                return;
            }
            result.set(l / rhs.l);
            return;
        }
        if (union == P_LD) {
            result.set(l / rhs.d);
            return;
        }
        if (union == P_DL) {
            result.set(l / (double) rhs.l);
            return;
        }
        if (union == P_DD) {
            result.set(l / rhs.d);
            return;
        }
        binaryOperatorError("/", rhs);
    }

    public void rem(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL) {
            if (rhs.l == 0L) {
                currentThread().error("modulo by zero");
                return;
            }
            result.set(l % rhs.l);
            return;
        }
        if (union == P_LD) {
            if (rhs.d == 0.0) {
                currentThread().error("modulo by zero");
                return;
            }
            result.set(l % rhs.d);
            return;
        }
        if (union == P_DL) {
            if (rhs.l == 0L) {
                currentThread().error("modulo by zero");
                return;
            }
            result.set(l % rhs.l);
            return;
        }
        if (union == P_DD) {
            if (rhs.d == 0.0) {
                currentThread().error("modulo by zero");
                return;
            }
            result.set(l % rhs.d);
            return;
        }
        binaryOperatorError("%", rhs);
    }

    public void shl(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL) {
            result.set(l << rhs.l);
            return;
        }
        binaryOperatorError("<<", rhs);
    }

    public void shr(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL) {
            result.set(l >> rhs.l);
            return;
        }
        binaryOperatorError(">>", rhs);
    }

    public void and(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL || union == P_BB) {
            result.set(l & rhs.l);
            return;
        }
        binaryOperatorError("&", rhs);
    }

    public void or(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL || union == P_BB) {
            result.set(l | rhs.l);
            return;
        }
        binaryOperatorError("|", rhs);
    }

    public void xor(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL || union == P_BB) {
            result.set(l ^ rhs.l);
            return;
        }
        binaryOperatorError("^", rhs);
    }

    private void binaryOperatorError(String operator, Address rhs) {
        currentThread().error("Cannot apply binary '%s' with %s and %s",
                operator, typeName(), rhs.typeName());
    }

    public void neg(Address result) { // -x
        if (type == LONG) {
            result.set(-l);
            return;
        }
        if (type == DOUBLE) {
            result.set(-d);
            return;
        }

        unaryOperatorError("-");
    }

    public void pos(Address result) { // +x
        if (type == LONG) {
            result.set(+l);
            return;
        }
        if (type == DOUBLE) {
            result.set(+d);
            return;
        }

        unaryOperatorError("+");
    }

    public void not(Address result) { // ~x
        if (type == LONG) {
            result.set(~l);
            return;
        }

        unaryOperatorError("~");
    }

    public void inc(Address result) { // -x
        if (type == LONG) {
            result.set(l + 1L);
            return;
        }
        if (type == DOUBLE) {
            result.set(d + 1.0);
            return;
        }

        unaryOperatorError("++");
    }

    public void dec(Address result) { // -x
        if (type == LONG) {
            result.set(l - 1L);
            return;
        }
        if (type == DOUBLE) {
            result.set(d - 1.0);
            return;
        }

        unaryOperatorError("--");
    }

    private void unaryOperatorError(String operator) {
        currentThread().error("Cannot apply unary '%s' with %s",
                operator, typeName());
    }

    public int compareShort(short value, int except) {
        if (type == LONG) {
            return Long.compare(l, value);
        }
        if (type == DOUBLE) {
            return Double.compare(d, value);
        }
        return except;
    }

    public int compare(Address rhs, int except) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL) {
            return Long.compare(l, rhs.l);
        }
        if (union == P_LD) {
            return Double.isNaN(rhs.d) ? except : Double.compare(l, rhs.d);
        }
        if (union == P_DL) {
            return Double.isNaN(d) ? except : Double.compare(d, rhs.l);
        }
        if (union == P_DD) {
            return Double.isNaN(d) || Double.isNaN(rhs.d) ? except : Double.compare(d, rhs.d);
        }
        if (union == P_SS) {
            return stringVal().isSame(rhs.stringVal()) ? 0 : except;
        }
        // todo: P_AA
        if (union == P_MM) {
            return mapValue().isSame(rhs.mapValue()) ? 0 : except;
        }
        // todo: P_II
        if (union == P_NN) {
            return 0; // null == null
        }
        return except;
    }

    public boolean isSame(Address that) {
        if (this == that) return true;
        int p = ValueType.pairOf(type, that.type);
        if (p == P_LL || p == P_BB)
            return l == that.l;
        if (p == P_LD)
            return l == that.d;
        if (p == P_DL)
            return d == that.l;
        if (p == P_SS || p == P_AA || p == P_MM || p == P_II)
            return a.equals(that.a);
        return p == P_NN; // null == null
    }

    @Override
    public int hashCode() {
        switch (type) {
            case UNDEFINED: throw new IllegalStateException("Trying calculate a hash-code of undefined type");
            case LONG: return Long.hashCode(longVal());
            case BOOLEAN: return Boolean.hashCode(booleanVal());
            case DOUBLE: return Double.hashCode(doubleVal());
            case STRING: return stringVal().hashCode();
            case ARRAY: throw new UnsupportedOperationException();
            case MAP: return mapValue().hashCode();
            case ITERATOR: throw new UnsupportedOperationException();
            case NULL: return 0;
            default: throw new IllegalStateException("Unknown type");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Очень маловероятно
        if (o == null || getClass() != o.getClass()) return false;
        return isSame((Address) o);
    }

    @Override
    public String toString() {
        switch (type) {
            case UNDEFINED: throw new IllegalStateException();
            case LONG: return Long.toString(longVal());
            case BOOLEAN: return Boolean.toString(booleanVal());
            case DOUBLE: return Double.toString(doubleVal());
            case STRING: return stringVal().toString();
            case ARRAY: throw new UnsupportedOperationException();
            case MAP: return mapValue().toString();
            case ITERATOR: throw new UnsupportedOperationException();
            case NULL: return "null";
            default: throw new IllegalStateException("Unknown type");
        }
    }
}
