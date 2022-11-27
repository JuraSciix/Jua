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

    public static Address[] allocateMemory(int count, int start) {
        if (count < 0 || count > 0xFFFF) {
            throw new IllegalArgumentException("Count: " + count);
        }
        if (start < 0 || start > count) {
            throw new IllegalArgumentException("Offset: " + start);
        }
        Address[] memory = new Address[count];

        for (int i = start; i < count; i++) {
            memory[i] = new Address();
        }

        return memory;
    }

    public static void arraycopy(Address[] src, int srcOffset, Address[] dst, int dstOffset, int count) {
        if (src == null) {
            throw new IllegalArgumentException("Source memory is null");
        }

        if (dst == null) {
            throw new IllegalArgumentException("Destination memory is null");
        }

        if (srcOffset < 0 || dstOffset < 0 || count < 0 || (srcOffset + count) >= src.length || (dstOffset + count) >= dst.length) {
            String message = String.format(
                    "Memory (length, offset): source (%d, %d), destination (%d, %d). Count: %d",
                    src.length, srcOffset, dst.length, dstOffset, count
            );
            throw new IllegalArgumentException(message);
        }

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
        switch (type) {
            case LONG: return l;
            case DOUBLE: return (long) d;
            case BOOLEAN: return l & 1L;
            case NULL: return 0L;
            default:
                badTypeConversion(LONG);
                return Long.MIN_VALUE;
        }
    }

    public boolean booleanVal() {
        switch (type) {
            case LONG:
            case BOOLEAN:
                return (l & 1L) != 0;
            case DOUBLE:
                return d != 0.0;
            case STRING:
                return stringVal().length() > 0;
            case MAP:
                return mapValue().size() > 0;
            case NULL:
                return false;
            default:
                badTypeConversion(BOOLEAN);
                return false;
        }
    }

    public double doubleVal() {
        switch (type) {
            case LONG: return l;
            case DOUBLE: return d;
            case BOOLEAN: return (l & 1L);
            case NULL: return 0.0;
            default:
                badTypeConversion(DOUBLE);
                return Double.NaN;
        }
    }

    public StringHeap stringVal() {
        switch (type) {
            case LONG:
                return new StringHeap().append(longVal());
            case DOUBLE:
                return new StringHeap().append(doubleVal());
            case BOOLEAN:
                return new StringHeap().append(booleanVal());
            case STRING:
                return (StringHeap) a;
            case NULL:
                return new StringHeap().appendNull();
            default:
                badTypeConversion(STRING);
                return StringHeap.temp();
        }
    }

    public MapHeap mapValue() {
        return (MapHeap) a;
    }

    public boolean testType(byte type) {
        if (type == this.type) {
            return true;
        }
        badTypeConversion(type);
        return false;
    }

    private void badTypeConversion(byte type) {
        currentThread().error("Cannot convert " + typeName() + " to " + nameOf(type));
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

    public boolean add(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL) {
            result.set(l + rhs.l);
            return true;
        }
        if (union == P_LD) {
            result.set(l + rhs.d);
            return true;
        }
        if (union == P_DL) {
            result.set(d + rhs.l);
            return true;
        }
        if (union == P_DD) {
            result.set(d + rhs.d);
            return true;
        }
        if (union == P_LS) {
            result.set(new StringHeap().append(longVal()).append(rhs.stringVal()));
            return true;
        }
        if (union == P_DS) {
            result.set(new StringHeap().append(doubleVal()).append(rhs.stringVal()));
            return true;
        }
        if (union == P_BS) {
            result.set(new StringHeap().append(booleanVal()).append(rhs.stringVal()));
            return true;
        }
        if (union == P_SS) {
            if (this == result) {
                stringVal().append(rhs.stringVal());
            } else {
                rhs.set(new StringHeap().append(stringVal()).append(rhs.stringVal()));
            }
            return true;
        }
        if (union == P_NS) {
            result.set(new StringHeap().appendNull().append(rhs.stringVal()));
            return true;
        }
        if (union == P_SL) {
            if (this == result) {
                stringVal().append(rhs.longVal());
            } else {
                rhs.set(new StringHeap(stringVal()).append(rhs.longVal()));
            }
            return true;
        }
        if (union == P_SD) {
            if (this == result) {
                stringVal().append(rhs.doubleVal());
            } else {
                rhs.set(new StringHeap(stringVal()).append(rhs.doubleVal()));
            }
            return true;
        }
        if (union == P_SB) {
            if (this == result) {
                stringVal().append(rhs.booleanVal());
            } else {
                rhs.set(new StringHeap(stringVal()).append(rhs.booleanVal()));
            }
            return true;
        }
        if (union == P_SN) {
            if (this == result) {
                stringVal().appendNull();
            } else {
                rhs.set(new StringHeap(stringVal()).appendNull());
            }
            return true;
        }
        return binaryOperatorError("+", rhs);
    }

    public boolean sub(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL) {
            result.set(l - rhs.l);
            return true;
        }
        if (union == P_LD) {
            result.set(l - rhs.d);
            return true;
        }
        if (union == P_DL) {
            result.set(l - rhs.l);
            return true;
        }
        if (union == P_DD) {
            result.set(l - rhs.d);
            return true;
        }
        // todo: arrays, maps
        return binaryOperatorError("-", rhs);
    }

    public boolean mul(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL) {
            result.set(l * rhs.l);
            return true;
        }
        if (union == P_LD) {
            result.set(l * rhs.d);
            return true;
        }
        if (union == P_DL) {
            result.set(l * rhs.l);
            return true;
        }
        if (union == P_DD) {
            result.set(l * rhs.d);
            return true;
        }
        return binaryOperatorError("*", rhs);
    }

    public boolean div(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL) {
            if (rhs.l == 0L) {
                currentThread().error("division by zero");
                return false;
            }
            result.set(l / rhs.l);
            return true;
        }
        if (union == P_LD) {
            result.set(l / rhs.d);
            return true;
        }
        if (union == P_DL) {
            result.set(d / (double) rhs.l);
            return true;
        }
        if (union == P_DD) {
            result.set(d / rhs.d);
            return true;
        }
        return binaryOperatorError("/", rhs);
    }

    public boolean rem(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL) {
            if (rhs.l == 0L) {
                currentThread().error("modulo by zero");
                return false;
            }
            result.set(l % rhs.l);
            return true;
        }
        if (union == P_LD) {
            if (rhs.d == 0.0) {
                currentThread().error("modulo by zero");
                return false;
            }
            result.set(l % rhs.d);
            return true;
        }
        if (union == P_DL) {
            if (rhs.l == 0L) {
                currentThread().error("modulo by zero");
                return false;
            }
            result.set(l % rhs.l);
            return true;
        }
        if (union == P_DD) {
            if (rhs.d == 0.0) {
                currentThread().error("modulo by zero");
                return false;
            }
            result.set(l % rhs.d);
            return true;
        }
        return binaryOperatorError("%", rhs);
    }

    public boolean shl(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL) {
            result.set(l << rhs.l);
            return true;
        }
        return binaryOperatorError("<<", rhs);
    }

    public boolean shr(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL) {
            result.set(l >> rhs.l);
            return true;
        }
        return binaryOperatorError(">>", rhs);
    }

    public boolean and(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL || union == P_BB) {
            result.set(l & rhs.l);
            return true;
        }
        return binaryOperatorError("&", rhs);
    }

    public boolean or(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL || union == P_BB) {
            result.set(l | rhs.l);
            return true;
        }
        return binaryOperatorError("|", rhs);
    }

    public boolean xor(Address rhs, Address result) {
        int union = pairOf(type, rhs.type);
        if (union == P_LL || union == P_BB) {
            result.set(l ^ rhs.l);
            return true;
        }
        return binaryOperatorError("^", rhs);
    }


    private boolean binaryOperatorError(String operator, Address rhs) {
        currentThread().error("Cannot apply binary '%s' with %s and %s",
                operator, typeName(), rhs.typeName());
        // Методы бинарных операций возвращают результат этой функции, чтобы сократить число строк =)
        return false;
    }

    public boolean neg(Address result) { // -x
        if (type == LONG) {
            result.set(-l);
            return true;
        }
        if (type == DOUBLE) {
            result.set(-d);
            return true;
        }

        return unaryOperatorError("-");
    }

    public boolean pos(Address result) { // +x
        if (type == LONG) {
            result.set(+l);
            return true;
        }
        if (type == DOUBLE) {
            result.set(+d);
            return true;
        }

        return unaryOperatorError("+");
    }

    public boolean not(Address result) { // ~x
        if (type == LONG) {
            result.set(~l);
            return true;
        }

        return unaryOperatorError("~");
    }

    public boolean inc(Address result) { // -x
        if (type == LONG) {
            result.set(l + 1L);
            return true;
        }
        if (type == DOUBLE) {
            result.set(d + 1.0);
            return true;
        }

        return unaryOperatorError("++");
    }

    public boolean dec(Address result) { // -x
        if (type == LONG) {
            result.set(l - 1L);
            return true;
        }
        if (type == DOUBLE) {
            result.set(d - 1.0);
            return true;
        }

        return unaryOperatorError("--");
    }

    private boolean unaryOperatorError(String operator) {
        currentThread().error("Cannot apply unary '%s' with %s", operator, typeName());
        // Методы унарных операций возвращают результат этой функции, чтобы сократить число строк =)
        return false;
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

    public int realCompare(Address rhs, int except) {
        if (type == STRING) {
            if (rhs.type == MAP) return except;
            return stringVal().compare(rhs.stringVal());
        }
        if (rhs.type == STRING) {
            if (rhs.type == MAP) return except;
            return stringVal().compare(rhs.stringVal());
        }
        int union = pairOf(type, rhs.type);
        if (union == P_LL || union == P_BB) return Long.compare(l, rhs.l);
        if (union == P_LD) return Double.isNaN(rhs.d) ? except : Double.compare(l, rhs.d);
        if (union == P_DL) return Double.isNaN(d) ? except : Double.compare(d, rhs.l);
        if (union == P_DD)
            return Double.isNaN(d) || Double.isNaN(rhs.d) ? except : Double.compare(d, rhs.d);
        if (union == P_MM)
            return mapValue().compare(rhs.mapValue(), except);
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
        if (p == P_SS || p == P_AA || p == P_MM || p == P_II)
            return a.equals(that.a);
        return p == P_NN; // null == null
    }

    @Override
    public int hashCode() {
        switch (type) {
            case UNDEFINED:
                throw new IllegalStateException("Trying calculate a hash-code of undefined type");
            case LONG:
                return Long.hashCode(longVal());
            case BOOLEAN:
                return Boolean.hashCode(booleanVal());
            case DOUBLE:
                return Double.hashCode(doubleVal());
            case STRING:
                return stringVal().hashCode();
            case ARRAY:
                throw new UnsupportedOperationException();
            case MAP:
                return mapValue().hashCode();
            case ITERATOR:
                throw new UnsupportedOperationException();
            case NULL:
                return 0;
            default:
                throw new IllegalStateException("Unknown type");
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
            case UNDEFINED:
                throw new IllegalStateException();
            case LONG:
                return Long.toString(longVal());
            case BOOLEAN:
                return Boolean.toString(booleanVal());
            case DOUBLE:
                return Double.toString(doubleVal());
            case STRING:
                return stringVal().toString();
            case ARRAY:
                throw new UnsupportedOperationException();
            case MAP:
                return mapValue().toString();
            case ITERATOR:
                throw new UnsupportedOperationException();
            case NULL:
                return "null";
            default:
                throw new IllegalStateException("Unknown type");
        }
    }
}
