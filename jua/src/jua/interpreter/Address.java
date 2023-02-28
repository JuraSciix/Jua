package jua.interpreter;

import jua.runtime.ValueType;
import jua.runtime.code.ConstantPool;
import jua.runtime.heap.Heap;
import jua.runtime.heap.ListHeap;
import jua.runtime.heap.MapHeap;
import jua.runtime.heap.StringHeap;
import jua.utils.Conversions;

import static jua.interpreter.InterpreterThread.threadError;
import static jua.runtime.ValueType.*;

public final class Address implements Comparable<Address>, ConstantPool.Entry {

    /** Тип текущего значения. */
    private byte type;

    private long l;
    private double d;
    private Heap a;

    /** Возвращает тип текущего значения. */
    public byte getType() { return type; }

    /** Возвращает название типа текущего значения. */
    public String getTypeName() { return ValueType.getTypeName(type); }

    /** Возвращает {@code true}, если текущее значение считается скалярным, иначе {@code false}. */
    public boolean isScalar() { return isTypeScalar(type); }

    /* * * * * * * * * * * * * * * * * * * *
     *               ГЕТТЕРЫ               *
     * * * * * * * * * * * * * * * * * * * */

    public long getLong() { return l; }

    public double getDouble() { return d; }

    public boolean getBoolean() { return Conversions.l2b(getLong()); }

    public Heap getHeap() { return a; }

    public StringHeap getStringHeap() { return (StringHeap) getHeap(); }

    public MapHeap getMapHeap() { return (MapHeap) getHeap(); }

    public ListHeap getListHeap() { return (ListHeap) getHeap(); }

    /* * * * * * * * * * * * * * * * * * * *
     *           ПРЕОБРАЗОВАНИЯ            *
     * * * * * * * * * * * * * * * * * * * */

    /** Преобразовывает значение в целочисленное 64-битное. */
    public boolean longVal(Address dst) {
        switch (type) {
            case NULL:
                dst.set(0L);
                return true;
            case LONG:
                dst.set(getLong());
                return true;
            case DOUBLE:
                dst.set((long) getDouble());
                return true;
            case BOOLEAN:
                dst.set(getLong() & 1L);
                return true;
            default:
                badTypeConversion(LONG);
                return false;
        }
    }

    /** Преобразовывает значение в вещественное 64-битное. */
    public boolean doubleVal(Address dst) {
        switch (type) {
            case NULL:
                dst.set(0.0);
                return true;
            case LONG:
                dst.set((double) getLong());
                return true;
            case DOUBLE:
                dst.set(getDouble());
                return true;
            case BOOLEAN:
                dst.set((double) (getLong() & 1L));
                return true;
            default:
                badTypeConversion(DOUBLE);
                return false;
        }
    }

    /** Преобразовывает значение в логическое. */
    public boolean booleanVal(Address dst) {
        dst.set(booleanVal());
        return true;
    }

    public boolean booleanVal() {
        switch (type) {
            case NULL:
                return false;
            case LONG:
            case BOOLEAN:
                return Conversions.l2b(getLong());
            case DOUBLE:
                return getDouble() != 0.0;
            case STRING:
                return getStringHeap().nonEmpty();
            case MAP:
                return getMapHeap().nonEmpty();
            default:
                // Любой валидный тип можно преобразовать в логический
                throw new AssertionError(getTypeName());
        }
    }

    public StringHeap stringVal() {
        switch (type) {
            case NULL:    return new StringHeap().appendNull();
            case LONG:    return new StringHeap().append(getLong());
            case DOUBLE:  return new StringHeap().append(getDouble());
            case BOOLEAN: return new StringHeap().append(getBoolean());
            case STRING:  return getStringHeap();
            default: throw new IllegalArgumentException("Unable to convert " + getTypeName() + " to string");
        }
    }

    public boolean stringVal(Address dst) {
        switch (type) {
            case NULL:
                dst.set(new StringHeap().appendNull());
                return true;
            case LONG:
                dst.set(new StringHeap().append(getLong()));
                return true;
            case DOUBLE:
                dst.set(new StringHeap().append(getDouble()));
                return true;
            case BOOLEAN:
                dst.set(new StringHeap().append(getBoolean()));
                return true;
            case STRING:
                dst.set(getStringHeap());
                return true;
            default:
                badTypeConversion(STRING);
                return false;
        }
    }

    public boolean mapValue(Address dst) {
        if (type == MAP) {
            dst.set(getMapHeap());
            return true;
        }
        badTypeConversion(MAP);
        return false;
    }

    public boolean testType(byte type) {
        if (type == this.type) {
            return true;
        }
        badTypeConversion(type);
        return false;
    }

    private void badTypeConversion(byte type) {
        threadError("Cannot convert %s to %s", getTypeName(), ValueType.getTypeName(type));
    }

    public boolean isValid() {
        return type != UNDEFINED;
    }

    public boolean isNull() {
        return type == NULL;
    }

    /* * * * * * * * * * * * * * * * * * * *
     *               СЕТТЕРЫ               *
     * * * * * * * * * * * * * * * * * * * */

    public void set(long _l) {
        type = LONG;
        l = _l;
    }

    public void set(boolean b) {
        type = BOOLEAN;
        l = Conversions.b2l(b);
    }

    public void set(double _d) {
        type = DOUBLE;
        d = _d;
    }

    public void set(StringHeap s) {
        type = STRING;
        a = s;
    }

    public void set(MapHeap m) {
        type = MAP;
        a = m;
    }

    public void set(ListHeap l) {
        type = LIST;
        a = l;
    }

    @Deprecated
    public void quickSet(Address source) {
        type = source.type;
        l = source.getLong();
        d = source.getDouble();
        a = source.getHeap();
    }

    public void set(Address source) {
        switch (source.type) {
            case NULL:
                setNull();
                break;
            case LONG:
                set(source.getLong());
                break;
            case DOUBLE:
                set(source.getDouble());
                break;
            case BOOLEAN:
                set(source.getBoolean());
                break;
            case STRING:
                set(source.getStringHeap().refCopy());
                break;
            case MAP:
                set(source.getMapHeap().refCopy());
                break;
            case LIST:
                set(source.getListHeap().refCopy());
                break;
            default:
                throw new AssertionError(source.type);
        }
    }

    public void clone(Address receiver) {
        switch (type) {
            case LONG:
                receiver.set(getLong());
                break;
            case DOUBLE:
                receiver.set(getDouble());
                break;
            case BOOLEAN:
                receiver.set(getBoolean());
                break;
            case STRING:
                receiver.set(getStringHeap().deepCopy());
                break;
            case MAP:
                receiver.set(getMapHeap().deepCopy());
                break;
            case LIST:
                receiver.set(getListHeap().deepCopy());
            case NULL:
                receiver.setNull();
                break;
            default:
                throw new AssertionError(type);
        }
    }

    public void setNull() {
        type = NULL;
    }

    public void reset() {
        type = UNDEFINED;
        a = null; // В помощь GC
    }

    public boolean hasType(byte type) {
        return getType() == type;
    }

    /* * * * * * * * * * * * * * * * * * * *
     *          БИНАРНЫЕ ОПЕРАЦИИ          *
     * * * * * * * * * * * * * * * * * * * */

    public boolean add(Address rhs, Address result) {
        int union = getTypeUnion(type, rhs.type);

        if (getTypeUnion(LONG, LONG) == union) {
            result.set(getLong() + rhs.getLong());
            return true;
        }

        if (getTypeUnion(LONG, DOUBLE) == union) {
            result.set(getLong() + rhs.getDouble());
            return true;
        }

        if (getTypeUnion(DOUBLE, LONG) == union) {
            result.set(getDouble() + rhs.getLong());
            return true;
        }

        if (getTypeUnion(DOUBLE, DOUBLE) == union) {
            result.set(getDouble() + rhs.getDouble());
            return true;
        }

        if (getTypeUnion(STRING, STRING) == union) {
            if (this == result) {
                getStringHeap().append(rhs.getStringHeap());
            } else {
                result.set(new StringHeap().append(getStringHeap()).append(getStringHeap()));
            }
            return true;
        }

        if (type == STRING) {
            Address tmp = new Address();
            if (!rhs.stringVal(tmp)) {
                return false;
            }
            result.set(new StringHeap().append(getStringHeap()).append(tmp.getStringHeap()));
            return true;
        }

        if (rhs.type == STRING) {
            Address tmp = new Address();
            if (!stringVal(tmp)) {
                return false;
            }
            result.set(new StringHeap().append(tmp.getStringHeap()).append(rhs.getStringHeap()));
            return true;
        }

        return binaryOperatorError("+", rhs);
    }

    public boolean sub(Address rhs, Address result) {
        int union = getTypeUnion(type, rhs.type);

        if (getTypeUnion(LONG, LONG) == union) {
            result.set(getLong() - rhs.getLong());
            return true;
        }

        if (getTypeUnion(LONG, DOUBLE) == union) {
            result.set((double) getLong() - rhs.getDouble());
            return true;
        }

        if (getTypeUnion(DOUBLE, LONG) == union) {
            result.set(getDouble() - (double) rhs.getLong());
            return true;
        }

        if (getTypeUnion(DOUBLE, DOUBLE) == union) {
            result.set(getDouble() - rhs.getDouble());
            return true;
        }

        return binaryOperatorError("-", rhs);
    }

    public boolean mul(Address rhs, Address result) {
        int union = getTypeUnion(type, rhs.type);

        if (getTypeUnion(LONG, LONG) == union) {
            result.set(getLong() * rhs.getLong());
            return true;
        }

        if (getTypeUnion(LONG, DOUBLE) == union) {
            result.set((double) getLong() * rhs.getDouble());
            return true;
        }

        if (getTypeUnion(DOUBLE, LONG) == union) {
            result.set(getDouble() * (double) rhs.getLong());
            return true;
        }

        if (getTypeUnion(DOUBLE, DOUBLE) == union) {
            result.set(getDouble() * rhs.getDouble());
            return true;
        }

        return binaryOperatorError("*", rhs);
    }

    public boolean div(Address rhs, Address result) {
        int union = getTypeUnion(type, rhs.type);

        if (getTypeUnion(LONG, LONG) == union) {
            if (rhs.getLong() == 0L) {
                threadError("integer division by zero");
                return false;
            }
            result.set(getLong() / rhs.getLong());
            return true;
        }

        if (getTypeUnion(LONG, DOUBLE) == union) {
            result.set((double) getLong() / rhs.getDouble());
            return true;
        }

        if (getTypeUnion(DOUBLE, LONG) == union) {
            result.set(getDouble() / (double) rhs.getLong());
            return true;
        }

        if (getTypeUnion(DOUBLE, DOUBLE) == union) {
            result.set(getDouble() / rhs.getDouble());
            return true;
        }

        return binaryOperatorError("/", rhs);
    }

    public boolean rem(Address rhs, Address result) {
        int union = getTypeUnion(type, rhs.type);

        if (getTypeUnion(LONG, LONG) == union) {
            if (rhs.getLong() == 0L) {
                threadError("modulo by zero");
                return false;
            }
            result.set(getLong() % rhs.getLong());
            return true;
        }

        if (getTypeUnion(LONG, DOUBLE) == union) {
            if (rhs.getDouble() == 0.0) {
                threadError("modulo by zero");
                return false;
            }
            result.set(getLong() % rhs.getDouble());
            return true;
        }

        if (getTypeUnion(DOUBLE, LONG) == union) {
            if (rhs.getLong() == 0L) {
                threadError("modulo by zero");
                return false;
            }
            result.set(getDouble() % rhs.getLong());
            return true;
        }

        if (getTypeUnion(DOUBLE, DOUBLE) == union) {
            if (rhs.getDouble() == 0.0) {
                threadError("modulo by zero");
                return false;
            }
            result.set(getDouble() % rhs.getDouble());
            return true;
        }

        return binaryOperatorError("%", rhs);
    }

    public boolean shl(Address rhs, Address result) {
        if (getTypeUnion(LONG, LONG) == getTypeUnion(type, rhs.type)) {
            result.set(getLong() << rhs.getLong());
            return true;
        }

        return binaryOperatorError("<<", rhs);
    }

    public boolean shr(Address rhs, Address result) {
        if (getTypeUnion(LONG, LONG) == getTypeUnion(type, rhs.type)) {
            result.set(getLong() >> rhs.getLong());
            return true;
        }

        return binaryOperatorError(">>", rhs);
    }

    public boolean and(Address rhs, Address result) {
        int union = getTypeUnion(type, rhs.type);

        if (getTypeUnion(LONG, LONG) == union) {
            result.set(getLong() & rhs.getLong());
            return true;
        }

        if (getTypeUnion(BOOLEAN, BOOLEAN) == union) {
            result.set(getBoolean() & rhs.getBoolean());
            return true;
        }

        return binaryOperatorError("&", rhs);
    }

    public boolean or(Address rhs, Address result) {
        int union = getTypeUnion(type, rhs.type);

        if (getTypeUnion(LONG, LONG) == union) {
            result.set(getLong() | rhs.getLong());
            return true;
        }

        if (getTypeUnion(BOOLEAN, BOOLEAN) == union) {
            result.set(getBoolean() | rhs.getBoolean());
            return true;
        }

        return binaryOperatorError("|", rhs);
    }

    public boolean xor(Address rhs, Address result) {
        int union = getTypeUnion(type, rhs.type);

        if (getTypeUnion(LONG, LONG) == union) {
            result.set(getLong() ^ rhs.getLong());
            return true;
        }

        if (getTypeUnion(BOOLEAN, BOOLEAN) == union) {
            result.set(getBoolean() ^ rhs.getBoolean());
            return true;
        }

        return binaryOperatorError("^", rhs);
    }

    private boolean binaryOperatorError(String operator, Address rhs) {
        threadError(
                "Cannot apply binary '%s' with %s and %s", operator, getTypeName(), rhs.getTypeName()
        );
        // Методы бинарных операций возвращают результат этой функции, чтобы сократить число строк =)
        return false;
    }

    /* * * * * * * * * * * * * * * * * * * *
     *          УНАРНЫЕ ОПЕРАЦИИ           *
     * * * * * * * * * * * * * * * * * * * */

    public boolean neg(Address result) { // -x
        if (type == LONG) {
            result.set(-getLong());
            return true;
        }

        if (type == DOUBLE) {
            result.set(-getDouble());
            return true;
        }

        return unaryOperatorError("-");
    }

    public boolean pos(Address result) { // +x
        if (type == LONG) {
            result.set(+getLong());
            return true;
        }

        if (type == DOUBLE) {
            result.set(+getDouble());
            return true;
        }

        return unaryOperatorError("+");
    }

    public boolean not(Address result) { // ~x
        if (type == LONG) {
            result.set(~getLong());
            return true;
        }

        return unaryOperatorError("~");
    }

    public boolean inc() {
        if (type == LONG) {
            l++;
            return true;
        }

        if (type == DOUBLE) {
            d++;
            return true;
        }

        return unaryOperatorError("++");
    }

    public boolean dec() {
        if (type == LONG) {
            l--;
            return true;
        }

        if (type == DOUBLE) {
            d--;
            return true;
        }

        return unaryOperatorError("--");
    }

    public boolean arrayInc(Address key, Address oldValueReceptor) {
        if (type == LIST) {
            int index = validateIndex(key, true);
            if (index >= 0) {
                Address element = getListHeap().get(index);
                oldValueReceptor.set(element);
                return element.inc();
            }
            return false;
        }
        if (type == MAP) {
            if (validateKey(key, true)) {
                Address element = getMapHeap().get(key);
                oldValueReceptor.set(element);
                return element.inc();
            }
            return false;
        }
        threadError("trying to load array-element from %s", getTypeName());
        return false;
    }

    public boolean arrayDec(Address key, Address oldValueReceptor) {
        if (type == LIST) {
            int index = validateIndex(key, true);
            if (index >= 0) {
                Address element = getListHeap().get(index);
                oldValueReceptor.set(element);
                return element.dec();
            }
            return false;
        }
        if (type == MAP) {
            if (validateKey(key, true)) {
                Address element = getMapHeap().get(key);
                oldValueReceptor.set(element);
                return element.dec();
            }
            return false;
        }
        threadError("trying to load array-element from %s", getTypeName());
        return false;
    }

    private boolean unaryOperatorError(String operator) {
        threadError("Cannot apply unary '%s' with %s", operator, getTypeName());
        // Методы унарных операций возвращают результат этой функции, чтобы сократить число строк =)
        return false;
    }

    public boolean store(Address key, Address value) {
        if (type == LIST) {
            int index = validateIndex(key, true);
            if (index >= 0) {
                getListHeap().set(index, value, null);
                return true;
            }
            return false;
        }
        if (type == MAP) {
            if (validateKey(key, false)) {
                getMapHeap().put(key, value);
                return true;
            }
            return false;
        }
        threadError("trying to load array-element from %s", getTypeName());
        return false;
    }

    public boolean load(Address key, Address receptor) {
        if (type == LIST) {
            int index = validateIndex(key, true);
            if (index >= 0) {
                receptor.set(getListHeap().get(index));
                return true;
            }
            return false;
        }
        if (type == MAP) {
            if (validateKey(key, true)) {
                receptor.set(getMapHeap().get(key));
                return true;
            }
            return false;
        }
        threadError("trying to load array-element from %s", getTypeName());
        return false;
    }

    public boolean contains(Address key, Address consumer) {
        if (type == LIST) {
            int index = validateIndex(key, false);
            if (index >= 0) {
                consumer.set(getListHeap().contains(key));
                return true;
            }
            return false;
        }
        if (type == MAP) {
            if (validateKey(key, false)) {
                consumer.set(getMapHeap().containsKey(key));
                return true;
            }
            return false;
        }
        threadError("trying to check array-element from %s", getTypeName());
        return false;
    }

    private int validateIndex(Address indexAddress, boolean validateBounds) {
        if (indexAddress.getType() == LONG) {
            long longIndex = indexAddress.getLong();
            if (!validateBounds ||
                    longIndex >= 0 && longIndex < getListHeap().length()) {
                return (int) longIndex;
            }
            threadError("index %d out of the list bounds %d", longIndex, getListHeap().length());
        } else {
            threadError("trying to access a list with non-integer key");
        }
        return -1;
    }

    private boolean validateKey(Address keyAddress, boolean checkContaining) {
        if (keyAddress.isScalar()) {
            if (!checkContaining || getMapHeap().containsKey(keyAddress)) {
                return true;
            }
            threadError("undefined map key: %s", keyAddress.toString());
        } else {
            threadError("trying to access a map with non-scalar key of type %s", keyAddress.getTypeName());
        }
        return false;
    }

    public boolean length(Address receptor) {
        if (type == STRING) {
            receptor.set(getStringHeap().size());
            return true;
        }

        if (type == MAP) {
            receptor.set(getMapHeap().size());
            return true;
        }

        if (type == LIST) {
            receptor.set(getListHeap().length());
            return true;
        }

        threadError("trying to calculate the length of %s", getTypeName());
        return false;
    }

    public boolean canBeComparedWith(Address rhs) {
        // Два значения одного типа всегда можно сравнивать.
        if (getType() == rhs.getType()) return true;
        // Поскольку язык динамически типизированный, нулём может оказаться любая переменная,
        // а значит нуль-проверка всегда имеет место быть.
        if (getType() == NULL || rhs.getType() == NULL) return true;
        // Типы значений, которые можно сравнивать со значениями других типов.
        int u = getTypeUnion(getType(), rhs.getType());
        if (u == getTypeUnion(LONG, DOUBLE)) return true;
        if (u == getTypeUnion(DOUBLE, LONG)) return true;
        return false;
    }

    public int weakCompare(Address rhs, int except) {
        int union = getTypeUnion(type, rhs.type);

        if (getTypeUnion(LONG, LONG) == union) {
            return Long.compare(getLong(), rhs.getLong());
        }

        if (getTypeUnion(LONG, DOUBLE) == union) {
            return Double.compare(getLong(), rhs.getDouble());
        }

        if (getTypeUnion(DOUBLE, LONG) == union) {
            return Double.compare(getDouble(), rhs.getLong());
        }

        if (getTypeUnion(DOUBLE, DOUBLE) == union) {
            if (Double.isNaN(getDouble()) || Double.isNaN(rhs.getDouble()))
                return except;
            else
                return Double.compare(getDouble(), rhs.getDouble());
        }

        if (getTypeUnion(STRING, STRING) == union) {
            return getStringHeap().compareTo(rhs.getStringHeap());
        }

        if (getTypeUnion(MAP, MAP) == union) {
            return getMapHeap().compare(rhs.getMapHeap(), except);
        }

        if (getTypeUnion(LIST, LIST) == union) {
            return getListHeap().compare(rhs.getListHeap(), except);
        }

        if (getTypeUnion(NULL, NULL) == union) {
            return 0;
        }

        return except;
    }

    public int quickConstCompare(short value, int except) {
        if (type == LONG) {
            return Long.compare(getLong(), value);
        }
        if (type == DOUBLE) {
            return Double.compare(getDouble(), value);
        }
        return except;
    }

    @Override
    public int compareTo(Address o) {
        if (this == o) return 0;
        int typeUnion = getTypeUnion(type, o.type);

        if (typeUnion == getTypeUnion(LONG, LONG) || typeUnion == getTypeUnion(BOOLEAN, BOOLEAN))
            return Long.compare(getLong(), o.getLong());

        if (typeUnion == getTypeUnion(LONG, DOUBLE) || typeUnion == getTypeUnion(BOOLEAN, DOUBLE))
            return Double.compare(getLong(), o.getDouble());

        if (typeUnion == getTypeUnion(LONG, STRING))
            return new StringHeap().append(getLong()).compareTo(o.getStringHeap());

        if (typeUnion == getTypeUnion(DOUBLE, LONG) || typeUnion == getTypeUnion(DOUBLE, BOOLEAN))
            return Double.compare(getDouble(), o.getLong());

        if (typeUnion == getTypeUnion(DOUBLE, DOUBLE))
            return Double.compare(getDouble(), o.getDouble());

        if (typeUnion == getTypeUnion(DOUBLE, STRING))
            return new StringHeap().append(getDouble()).compareTo(o.getStringHeap());

        if (typeUnion == getTypeUnion(STRING, LONG))
            return getStringHeap().compareTo(new StringHeap().append(getLong()));

        if (typeUnion == getTypeUnion(STRING, DOUBLE))
            return getStringHeap().compareTo(new StringHeap().append(getBoolean()));

        if (typeUnion == getTypeUnion(STRING, BOOLEAN))
            return getStringHeap().compareTo(new StringHeap().append(getBoolean()));

        if (typeUnion == getTypeUnion(STRING, STRING))
            return getStringHeap().compareTo(o.getStringHeap());

        throw new IllegalArgumentException("Unable to compare " + getTypeName() + " with " + o.getTypeName());
    }

    @Override
    public int hashCode() {
        switch (type) {
            case NULL:    return 0;
            case LONG:    return Long.hashCode(getLong());
            case BOOLEAN: return Boolean.hashCode(getBoolean());
            case DOUBLE:  return Double.hashCode(getDouble());
            case STRING:  return getStringHeap().hashCode();
            case MAP:     return getMapHeap().hashCode();
            case LIST:    return getListHeap().hashCode();
            default:      throw new AssertionError(type);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Очень маловероятно
        if (o == null || getClass() != o.getClass()) return false;
        return weakCompare((Address) o, Integer.MIN_VALUE) == 0;
    }

    @Override
    public String toString() {
        // Этот метод не связан с рантаймом Jua
        switch (type) {
            case NULL:    return "null";
            case LONG:    return Long.toString(getLong());
            case DOUBLE:  return Double.toString(getDouble());
            case BOOLEAN: return Boolean.toString(getBoolean());
            case STRING:  return '"' + getStringHeap().toString() + '"';
            case MAP:     return getMapHeap().toString();
            case LIST:    return getListHeap().toString();
            default:      throw new AssertionError(type);
        }
    }
}
