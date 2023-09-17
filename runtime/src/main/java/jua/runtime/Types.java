package jua.runtime;

import jua.runtime.heap.ListHeap;
import jua.runtime.heap.MapHeap;
import jua.runtime.heap.StringHeap;

/**
 * Класс, определяющий типы данных Jua.
 */
public class Types {

    /** Неопределенный тип. Работа с ним всегда должна приводить к ошибке. */
    public static final byte T_UNDEFINED = 0;

    public static final byte T_NULL = 1;
    public static final byte T_INT = 2;
    public static final byte T_FLOAT = 3;
    public static final byte T_BOOLEAN = 4;
    public static final byte T_STRING = 5;
    public static final byte T_MAP = 6; // todo: Переименовать этот тип в object.
    public static final byte T_LIST = 7;

    /** Возвращает имя типа. */
    public static String getTypeName(byte type) {
        switch (type) {
            case T_NULL:    return "null";
            case T_INT:     return "int";
            case T_FLOAT:   return "float";
            case T_BOOLEAN: return "boolean";
            case T_STRING:  return "string";
            case T_MAP:     return "map";
            case T_LIST:    return "list";
            default:        return "UNDEFINED";
        }
    }

    /** Возвращает скалярное объединение двух типов. */
    public static int getTypeUnion(byte lhs, byte rhs) {
        return lhs | (rhs << 4);
    }

    public static boolean isTypeScalar(byte type) {
        return type >= T_INT && type <= T_STRING;
    }

    public static boolean l2b(long l) {
        return l != 0L;
    }

    public static String l2s(long l) {
        return Long.toString(l);
    }

    /**
     * Отражает вещественное число логическим значением.
     * Возвращает {@code false} при равенстве числа: {@code -0.0}, {@code 0.0}, {@code NaN};
     * во всех остальных возвращает {@code true}.
     *
     * @param d Вещественное число.
     * @return Логическое значение
     */
    public static boolean d2b(double d) {
        return !Double.isNaN(d) && d != 0.0; // d2b(NaN) != true
    }

    public static String d2s(double d) {
        return Double.toString(d);
    }

    public static long b2l(boolean b) {
        return b ? 1L : 0L;
    }

    public static double b2d(boolean b) {
        return b ? 1.0 : 0.0;
    }

    public static String b2s(boolean b) {
        return Boolean.toString(b);
    }

    public static boolean s2b(StringHeap s) {
        return s.nonEmpty();
    }

    public static boolean m2b(MapHeap m) {
        return m.nonEmpty();
    }

    public static boolean e2b(ListHeap li) {
        return li.nonEmpty();
    }

    public static int hashOfLong(long lval) {
        return Long.hashCode(lval);
    }

    public static int hashOfDouble(double dval) {
        return Double.hashCode(dval);
    }

    public static int hashOfBoolean(boolean bval) {
        return Boolean.hashCode(bval);
    }

    public static int hashOfString(StringHeap s) {
        return s.hashCode();
    }

    public static int hashOfMap(MapHeap m) {
        return m.hashCode();
    }

    public static int hashOfList(ListHeap l) {
        return l.hashCode();
    }
}
