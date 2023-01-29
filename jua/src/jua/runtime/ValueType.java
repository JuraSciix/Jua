package jua.runtime;

public interface ValueType {

    /** Неопределенный тип. Работа с ним всегда должна приводить к ошибке. */
    byte UNDEFINED = 0;

    byte NULL      = 1;
    byte LONG      = 2;
    byte DOUBLE    = 3;
    byte BOOLEAN   = 4;
    byte STRING    = 5;
    byte MAP       = 6;
    byte LIST      = 7;

    /** Возвращает имя типа. */
    static String getTypeName(byte type) {
        switch (type) {
            case NULL:    return "null";
            case LONG:    return "int";
            case DOUBLE:  return "float";
            case BOOLEAN: return "boolean";
            case STRING:  return "string";
            case MAP:     return "map";
            case LIST:    return "list";
            default: throw new AssertionError(type);
        }
    }

    /** Возвращает скалярное объединение двух типов. */
    static int getTypeUnion(byte lhs, byte rhs) {
        return lhs | (rhs << 4);
    }

    static boolean isTypeScalar(byte type) {
        return type >= LONG && type <= STRING;
    }
}
