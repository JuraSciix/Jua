package jua.runtime.interpreter;

import jua.runtime.Operations;
import jua.runtime.Types;
import jua.runtime.heap.Heap;
import jua.runtime.heap.ListHeap;
import jua.runtime.heap.StringHeap;

import static jua.runtime.Operations.toResultCode;
import static jua.runtime.interpreter.InterpreterThread.threadError;
import static jua.runtime.Types.*;

public final class Address implements Comparable<Address> {

    /** Тип текущего значения. */
    private byte type;

    private long l;
    private double d;
    private Heap a;

    /** Возвращает тип текущего значения. */
    public byte getType() { return type; }

    /** Возвращает название типа текущего значения. */
    public String getTypeName() { return Types.getTypeName(type); }

    /** Возвращает {@code true}, если текущее значение считается скалярным, иначе {@code false}. */
    public boolean isScalar() { return isTypeScalar(type); }

    /* * * * * * * * * * * * * * * * * * * *
     *               ГЕТТЕРЫ               *
     * * * * * * * * * * * * * * * * * * * */

    public long getLong() { return l; }

    public double getDouble() { return d; }

    public boolean getBoolean() {
        return l2b(getLong());
    }

    public Heap getHeap() { return a; }

    public StringHeap getStringHeap() { return (StringHeap) getHeap(); }

    public ListHeap getListHeap() { return (ListHeap) getHeap(); }

    /* * * * * * * * * * * * * * * * * * * *
     *           ПРЕОБРАЗОВАНИЯ            *
     * * * * * * * * * * * * * * * * * * * */

    /** Преобразовывает значение в целочисленное 64-битное. */
    public boolean longVal(Address dst) {
        switch (type) {
            case T_NULL:
                dst.set(0L);
                return true;
            case T_INT:
                dst.set(getLong());
                return true;
            case T_FLOAT:
                dst.set((long) getDouble());
                return true;
            case T_BOOLEAN:
                dst.set(b2l(getBoolean()));
                return true;
            default:
                badTypeConversion(T_INT);
                return false;
        }
    }

    /** Преобразовывает значение в вещественное 64-битное. */
    public boolean doubleVal(Address dst) {
        switch (type) {
            case T_NULL:
                dst.set(0.0);
                return true;
            case T_INT:
                dst.set((double) getLong());
                return true;
            case T_FLOAT:
                dst.set(getDouble());
                return true;
            case T_BOOLEAN:
                dst.set(b2d(getBoolean()));
                return true;
            default:
                badTypeConversion(T_FLOAT);
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
            case T_NULL:
                return false;
            case T_INT:
            case T_BOOLEAN:
                return l2b(getLong());
            case T_FLOAT:
                return d2b(getDouble());
            case T_STRING:
                return s2b(getStringHeap());
            case T_LIST:
                return e2b(getListHeap());
            default:
                // Любой валидный тип можно преобразовать в логический
                throw new AssertionError(getTypeName());
        }
    }

    public StringHeap stringVal() {
        switch (type) {
            case T_NULL:    return new StringHeap().appendNull();
            case T_INT:    return new StringHeap().append(getLong());
            case T_FLOAT:  return new StringHeap().append(getDouble());
            case T_BOOLEAN: return new StringHeap().append(getBoolean());
            case T_STRING:  return getStringHeap();
            default: throw new IllegalArgumentException("Unable to convert " + getTypeName() + " to string");
        }
    }

    public boolean stringVal(Address dst) {
        switch (type) {
            case T_NULL:
                dst.set(new StringHeap().appendNull());
                return true;
            case T_INT:
                dst.set(new StringHeap().append(getLong()));
                return true;
            case T_FLOAT:
                dst.set(new StringHeap().append(getDouble()));
                return true;
            case T_BOOLEAN:
                dst.set(new StringHeap().append(getBoolean()));
                return true;
            case T_STRING:
                dst.set(getStringHeap());
                return true;
            default:
                badTypeConversion(T_STRING);
                return false;
        }
    }

    public boolean testType(byte type) {
        if (type == this.type) {
            return true;
        }
        badTypeConversion(type);
        return false;
    }

    private void badTypeConversion(byte type) {
        threadError("Cannot convert %s to %s", getTypeName(), Types.getTypeName(type));
    }

    public boolean isValid() {
        return type != T_UNDEFINED;
    }

    public boolean isNull() {
        return type == T_NULL;
    }

    /* * * * * * * * * * * * * * * * * * * *
     *               СЕТТЕРЫ               *
     * * * * * * * * * * * * * * * * * * * */

    public void set(long _l) {
        type = T_INT;
        l = _l;
    }

    public void set(boolean b) {
        type = T_BOOLEAN;
        l = b2l(b);
    }

    public void set(double _d) {
        type = T_FLOAT;
        d = _d;
    }

    public void set(StringHeap s) {
        type = T_STRING;
        a = s;
    }

    public void set(ListHeap l) {
        type = T_LIST;
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
            case T_NULL:
                setNull();
                break;
            case T_INT:
                set(source.getLong());
                break;
            case T_FLOAT:
                set(source.getDouble());
                break;
            case T_BOOLEAN:
                set(source.getBoolean());
                break;
            case T_STRING:
                set(source.getStringHeap().refCopy());
                break;
            case T_LIST:
                set(source.getListHeap().refCopy());
                break;
            default:
                throw new AssertionError(source.type);
        }
    }

    public void clone(Address receiver) {
        switch (type) {
            case T_INT:
                receiver.set(getLong());
                break;
            case T_FLOAT:
                receiver.set(getDouble());
                break;
            case T_BOOLEAN:
                receiver.set(getBoolean());
                break;
            case T_STRING:
                receiver.set(getStringHeap().deepCopy());
                break;
            case T_LIST:
                receiver.set(getListHeap().deepCopy());
                break;
            case T_NULL:
                receiver.setNull();
                break;
            default:
                throw new AssertionError(type);
        }
    }

    public void setNull() {
        type = T_NULL;
    }

    public void reset() {
        type = T_UNDEFINED;
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

        if (getTypeUnion(T_INT, T_INT) == union) {
            result.set(getLong() + rhs.getLong());
            return true;
        }

        if (getTypeUnion(T_INT, T_FLOAT) == union) {
            result.set(getLong() + rhs.getDouble());
            return true;
        }

        if (getTypeUnion(T_FLOAT, T_INT) == union) {
            result.set(getDouble() + rhs.getLong());
            return true;
        }

        if (getTypeUnion(T_FLOAT, T_FLOAT) == union) {
            result.set(getDouble() + rhs.getDouble());
            return true;
        }

        if (getTypeUnion(T_STRING, T_STRING) == union) {
            if (this == result) {
                getStringHeap().append(rhs.getStringHeap());
            } else {
                result.set(new StringHeap().append(getStringHeap()).append(getStringHeap()));
            }
            return true;
        }

        if (type == T_STRING) {
            Address tmp = new Address();
            if (!rhs.stringVal(tmp)) {
                return false;
            }
            result.set(new StringHeap().append(getStringHeap()).append(tmp.getStringHeap()));
            return true;
        }

        if (rhs.type == T_STRING) {
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

        if (getTypeUnion(T_INT, T_INT) == union) {
            result.set(getLong() - rhs.getLong());
            return true;
        }

        if (getTypeUnion(T_INT, T_FLOAT) == union) {
            result.set((double) getLong() - rhs.getDouble());
            return true;
        }

        if (getTypeUnion(T_FLOAT, T_INT) == union) {
            result.set(getDouble() - (double) rhs.getLong());
            return true;
        }

        if (getTypeUnion(T_FLOAT, T_FLOAT) == union) {
            result.set(getDouble() - rhs.getDouble());
            return true;
        }

        return binaryOperatorError("-", rhs);
    }

    public boolean mul(Address rhs, Address result) {
        int union = getTypeUnion(type, rhs.type);

        if (getTypeUnion(T_INT, T_INT) == union) {
            result.set(getLong() * rhs.getLong());
            return true;
        }

        if (getTypeUnion(T_INT, T_FLOAT) == union) {
            result.set((double) getLong() * rhs.getDouble());
            return true;
        }

        if (getTypeUnion(T_FLOAT, T_INT) == union) {
            result.set(getDouble() * (double) rhs.getLong());
            return true;
        }

        if (getTypeUnion(T_FLOAT, T_FLOAT) == union) {
            result.set(getDouble() * rhs.getDouble());
            return true;
        }

        return binaryOperatorError("*", rhs);
    }

    public boolean div(Address rhs, Address result) {
        int union = getTypeUnion(type, rhs.type);

        if (getTypeUnion(T_INT, T_INT) == union) {
            if (rhs.getLong() == 0L) {
                threadError("integer division by zero");
                return false;
            }
            result.set(getLong() / rhs.getLong());
            return true;
        }

        if (getTypeUnion(T_INT, T_FLOAT) == union) {
            result.set((double) getLong() / rhs.getDouble());
            return true;
        }

        if (getTypeUnion(T_FLOAT, T_INT) == union) {
            result.set(getDouble() / (double) rhs.getLong());
            return true;
        }

        if (getTypeUnion(T_FLOAT, T_FLOAT) == union) {
            result.set(getDouble() / rhs.getDouble());
            return true;
        }

        return binaryOperatorError("/", rhs);
    }

    public boolean rem(Address rhs, Address result) {
        int union = getTypeUnion(type, rhs.type);

        if (getTypeUnion(T_INT, T_INT) == union) {
            if (rhs.getLong() == 0L) {
                threadError("modulo by zero");
                return false;
            }
            result.set(getLong() % rhs.getLong());
            return true;
        }

        if (getTypeUnion(T_INT, T_FLOAT) == union) {
            if (rhs.getDouble() == 0.0) {
                threadError("modulo by zero");
                return false;
            }
            result.set(getLong() % rhs.getDouble());
            return true;
        }

        if (getTypeUnion(T_FLOAT, T_INT) == union) {
            if (rhs.getLong() == 0L) {
                threadError("modulo by zero");
                return false;
            }
            result.set(getDouble() % rhs.getLong());
            return true;
        }

        if (getTypeUnion(T_FLOAT, T_FLOAT) == union) {
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
        if (getTypeUnion(T_INT, T_INT) == getTypeUnion(type, rhs.type)) {
            result.set(getLong() << rhs.getLong());
            return true;
        }

        return binaryOperatorError("<<", rhs);
    }

    public boolean shr(Address rhs, Address result) {
        if (getTypeUnion(T_INT, T_INT) == getTypeUnion(type, rhs.type)) {
            result.set(getLong() >> rhs.getLong());
            return true;
        }

        return binaryOperatorError(">>", rhs);
    }

    public boolean and(Address rhs, Address result) {
        int union = getTypeUnion(type, rhs.type);

        if (getTypeUnion(T_INT, T_INT) == union) {
            result.set(getLong() & rhs.getLong());
            return true;
        }

        if (getTypeUnion(T_BOOLEAN, T_BOOLEAN) == union) {
            result.set(getBoolean() & rhs.getBoolean());
            return true;
        }

        return binaryOperatorError("&", rhs);
    }

    public boolean or(Address rhs, Address result) {
        int union = getTypeUnion(type, rhs.type);

        if (getTypeUnion(T_INT, T_INT) == union) {
            result.set(getLong() | rhs.getLong());
            return true;
        }

        if (getTypeUnion(T_BOOLEAN, T_BOOLEAN) == union) {
            result.set(getBoolean() | rhs.getBoolean());
            return true;
        }

        return binaryOperatorError("|", rhs);
    }

    public boolean xor(Address rhs, Address result) {
        int union = getTypeUnion(type, rhs.type);

        if (getTypeUnion(T_INT, T_INT) == union) {
            result.set(getLong() ^ rhs.getLong());
            return true;
        }

        if (getTypeUnion(T_BOOLEAN, T_BOOLEAN) == union) {
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
        if (type == T_INT) {
            result.set(-getLong());
            return true;
        }

        if (type == T_FLOAT) {
            result.set(-getDouble());
            return true;
        }

        return unaryOperatorError("-");
    }

    public boolean pos(Address result) { // +x
        if (type == T_INT) {
            result.set(+getLong());
            return true;
        }

        if (type == T_FLOAT) {
            result.set(+getDouble());
            return true;
        }

        return unaryOperatorError("+");
    }

    public boolean not(Address result) { // ~x
        if (type == T_INT) {
            result.set(~getLong());
            return true;
        }

        return unaryOperatorError("~");
    }

    public boolean inc() {
        if (type == T_INT) {
            l++;
            return true;
        }

        if (type == T_FLOAT) {
            d++;
            return true;
        }

        return unaryOperatorError("++");
    }

    public boolean dec() {
        if (type == T_INT) {
            l--;
            return true;
        }

        if (type == T_FLOAT) {
            d--;
            return true;
        }

        return unaryOperatorError("--");
    }

    public boolean arrayInc(Address key, Address oldValueReceptor) {
        if (type == T_LIST) {
            int index = validateIndex(key, true);
            if (index >= 0) {
                Address element = getListHeap().get(index);
                oldValueReceptor.set(element);
                return element.inc();
            }
            return false;
        }
        threadError("trying to increment array-element of %s", getTypeName());
        return false;
    }

    public boolean arrayDec(Address key, Address oldValueReceptor) {
        if (type == T_LIST) {
            int index = validateIndex(key, true);
            if (index >= 0) {
                Address element = getListHeap().get(index);
                oldValueReceptor.set(element);
                return element.dec();
            }
            return false;
        }
        threadError("trying to decrement array-element of %s", getTypeName());
        return false;
    }

    private boolean unaryOperatorError(String operator) {
        threadError("Cannot apply unary '%s' with %s", operator, getTypeName());
        // Методы унарных операций возвращают результат этой функции, чтобы сократить число строк =)
        return false;
    }

    public boolean store(Address key, Address value) {
        if (type == T_LIST) {
            int index = validateIndex(key, true);
            if (index >= 0) {
                getListHeap().set(index, value, null);
                return true;
            }
            return false;
        }
        threadError("trying to store array-element to %s", getTypeName());
        return false;
    }

    public boolean load(Address key, Address receptor) {
        if (type == T_LIST) {
            int index = validateIndex(key, true);
            if (index >= 0) {
                receptor.set(getListHeap().get(index));
                return true;
            }
            return false;
        }
        threadError("trying to load array-element from %s", getTypeName());
        return false;
    }

    public int contains(Address key) {
        if (type == T_LIST) {
            int index = validateIndex(key, false);
            if (index >= 0) {
                return toResultCode(getListHeap().contains(key));
            }
            return Operations.RESULT_FAILURE;
        }
        threadError("trying to check array-element from %s", getTypeName());
        return Operations.RESULT_FAILURE;
    }

    private int validateIndex(Address indexAddress, boolean validateBounds) {
        if (indexAddress.getType() == T_INT) {
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

    public boolean length(Address receptor) {
        if (type == T_STRING) {
            receptor.set(getStringHeap().length());
            return true;
        }

        if (type == T_LIST) {
            receptor.set(getListHeap().length());
            return true;
        }

        threadError("trying to calculate the length of %s", getTypeName());
        return false;
    }

    @Deprecated
    public boolean canBeComparedWith(Address rhs) {
        // Два значения одного типа всегда можно сравнивать.
        if (getType() == rhs.getType()) return true;
        // Поскольку язык динамически типизированный, нулём может оказаться любая переменная,
        // а значит нуль-проверка всегда имеет место быть.
        if (getType() == T_NULL || rhs.getType() == T_NULL) return true;
        // Типы значений, которые можно сравнивать со значениями других типов.
        int u = getTypeUnion(getType(), rhs.getType());
        if (u == getTypeUnion(T_INT, T_FLOAT)) return true;
        if (u == getTypeUnion(T_FLOAT, T_INT)) return true;
        return false;
    }

    @Override
    public int compareTo(Address o) {
        if (this == o) return 0;
        int typeUnion = getTypeUnion(type, o.type);

        if (typeUnion == getTypeUnion(T_INT, T_INT) || typeUnion == getTypeUnion(T_BOOLEAN, T_BOOLEAN))
            return Long.compare(getLong(), o.getLong());

        if (typeUnion == getTypeUnion(T_INT, T_FLOAT) || typeUnion == getTypeUnion(T_BOOLEAN, T_FLOAT))
            return Double.compare(getLong(), o.getDouble());

        if (typeUnion == getTypeUnion(T_INT, T_STRING))
            return new StringHeap().append(getLong()).compareTo(o.getStringHeap());

        if (typeUnion == getTypeUnion(T_FLOAT, T_INT) || typeUnion == getTypeUnion(T_FLOAT, T_BOOLEAN))
            return Double.compare(getDouble(), o.getLong());

        if (typeUnion == getTypeUnion(T_FLOAT, T_FLOAT))
            return Double.compare(getDouble(), o.getDouble());

        if (typeUnion == getTypeUnion(T_FLOAT, T_STRING))
            return new StringHeap().append(getDouble()).compareTo(o.getStringHeap());

        if (typeUnion == getTypeUnion(T_STRING, T_INT))
            return getStringHeap().compareTo(new StringHeap().append(getLong()));

        if (typeUnion == getTypeUnion(T_STRING, T_FLOAT))
            return getStringHeap().compareTo(new StringHeap().append(getBoolean()));

        if (typeUnion == getTypeUnion(T_STRING, T_BOOLEAN))
            return getStringHeap().compareTo(new StringHeap().append(getBoolean()));

        if (typeUnion == getTypeUnion(T_STRING, T_STRING))
            return getStringHeap().compareTo(o.getStringHeap());

        throw new IllegalArgumentException("Unable to compare " + getTypeName() + " with " + o.getTypeName());
    }

    public int fastCompareWith(Address a, int unexpected) {
        int union = getTypeUnion(type, a.type);

        if (getTypeUnion(T_INT, T_INT) == union) {
            return Long.compare(getLong(), a.getLong());
        }

        if (getTypeUnion(T_INT, T_FLOAT) == union) {
            return Double.compare(getLong(), a.getDouble());
        }

        if (getTypeUnion(T_FLOAT, T_INT) == union) {
            return Double.compare(getDouble(), a.getLong());
        }

        if (getTypeUnion(T_FLOAT, T_FLOAT) == union) {
            if (Double.isNaN(getDouble()) || Double.isNaN(a.getDouble()))
                return unexpected;
            else
                return Double.compare(getDouble(), a.getDouble());
        }

        if (getTypeUnion(T_STRING, T_STRING) == union) {
            return getStringHeap().compareTo(a.getStringHeap());
        }

        if (getTypeUnion(T_LIST, T_LIST) == union) {
            return getListHeap().fastCompare(a.getListHeap(), unexpected);
        }

        if (getTypeUnion(T_NULL, T_NULL) == union) {
            return 0;
        }

        return unexpected;
    }

    /**
     * Возвращает debug-идентификатор, длина которого не больше 5 символов.
     */
    public String id() {
        char c;
        switch (type) {
            case T_UNDEFINED: return "~";
            case T_NULL:      return "nil";
            case T_INT:       c = 'N'; break;
            case T_FLOAT:     c = 'F'; break;
            case T_BOOLEAN:   c = 'Z'; break;
            case T_STRING:    c = 'S'; break;
            case T_LIST:      c = 'L'; break;
            default: throw new AssertionError(type);
        }
        int h = hashCode();
        h = (h ^ (h >> 16)) & 0xffff;
        return c + Integer.toHexString(h);
    }

    @Override
    public int hashCode() {
        switch (type) {
            case T_NULL:    return 0;
            case T_INT:     return hashOfLong(getLong());
            case T_FLOAT:   return hashOfDouble(getDouble());
            case T_BOOLEAN: return hashOfBoolean(getBoolean());
            case T_STRING:  return hashOfString(getStringHeap());
            case T_LIST:    return hashOfList(getListHeap());
            default: throw new AssertionError(type);
        }
    }

    /**
     * Сравнивает адреса по ссылкам. Для undefined и скалярных типов возвращает {@code false}.
     * Возвращает {@code true} если оба адреса - ссылочные, и ссылаются на один объект.
     */
    public boolean isEqualRefs(Address other) {
        if (type != other.type) return false; // Разные типы = разные ссылки
        if (isTypeScalar(type)) return false; // Скалярный тип это не ссылка...
        if (hasType(T_UNDEFINED)) return false; // Неопределенность это тоже не ссылка...
        return this.a == other.a;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Очень маловероятно
        if (o == null || getClass() != o.getClass()) return false;
        return fastCompareWith((Address) o, Integer.MIN_VALUE) == 0;
    }

    public String toBeautifulString() {
        switch (type) {
            case T_NULL:      return Types.getTypeName(T_NULL);
            case T_INT:       return Long.toString(getLong());
            case T_FLOAT:     return Double.toString(getDouble());
            case T_BOOLEAN:   return Boolean.toString(getBoolean());
            case T_STRING:    return '"' + getStringHeap().toString() + '"';
            case T_LIST:      return  getListHeap().toString();
            case T_UNDEFINED: // fallthrough
            default: throw new AssertionError(type);
        }
    }

    /**
     * @deprecated Use {@link AddressSupport#toJavaObject(Address)}.
     */
    public Object toObject() {
        return AddressSupport.toJavaObject(this);
    }

    /**
     * Возвращает сериализованное представление адреса.
     * Первым символом является идентификатор типа,
     * за ним следует значение.
     *
     * @return Сериализованное представление адреса.
     */
    @Override
    public String toString() {
        // Этот метод не связан с рантаймом Jua
        switch (type) {
            case T_UNDEFINED: return "~";
            case T_NULL:      return "<null>";
            case T_INT:       return "L" + getLong();
            case T_FLOAT:     return "D" + getDouble();
            case T_BOOLEAN:   return "B" + getBoolean();
            case T_STRING:    return "S" + getStringHeap();
            case T_LIST:      return "E" + getListHeap();
            default: throw new AssertionError(type);
        }
    }
}
