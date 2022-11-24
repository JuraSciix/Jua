package jua.runtime.heap;

import jua.interpreter.Address;
import jua.interpreter.InterpreterError;

import java.util.Map;
import java.util.Set;

/**
 * @deprecated Планируется переход на {@link jua.interpreter.Address} с {@link Heap}.
 */
@Deprecated
public abstract class Operand {

    public enum Type {

        LONG("int"),
        DOUBLE("float"),
        BOOLEAN("boolean"),
        STRING("string"),
        NULL("<null>"),
        MAP("array");

        public final String name;

        Type(String name) {
            this.name = name;
        }

        // Note: NULL.sigc() returns '<', so do not use it.
        public char sigc() { return name.charAt(0); }

        public boolean isScalar() {
            return ordinal() <= STRING.ordinal();
        }
    }

    public abstract Type type();

    public final boolean is(Type type) {
        return type() == type;
    }

    public boolean isMap() {
        return is(Type.MAP);
    }

    public boolean isBoolean() {
        return is(Type.BOOLEAN);
    }

    public boolean isDouble() {
        return is(Type.DOUBLE);
    }

    public boolean isLong() {
        return is(Type.LONG);
    }

    public boolean isNull() {
        return is(Type.NULL);
    }

    public boolean isNumber() {
        return false;
    }

    public boolean isString() {
        return false;
    }

    @Deprecated
    public boolean canBeMap() {
        return false;
    }

    public boolean canBeBoolean() {
        return false;
    }

    public boolean canBeFloat() {
        return false;
    }

    public boolean canBeInt() {
        return false;
    }

    public boolean canBeString() {
        return false;
    }

    public long longValue() {
        throw InterpreterError.inconvertibleTypes(type(), Type.LONG);
    }

    public double doubleValue() {
        throw InterpreterError.inconvertibleTypes(type(), Type.DOUBLE);
    }

    public boolean booleanValue() {
        throw InterpreterError.inconvertibleTypes(type(), Type.BOOLEAN);
    }

    public String stringValue() {
        throw InterpreterError.inconvertibleTypes(type(), Type.STRING);
    }

    @Deprecated
    public Array arrayValue() {
        throw InterpreterError.inconvertibleTypes(type(), Type.MAP);
    }

    @Override
    public Object clone() {
        if (!(this instanceof Cloneable)) {
            return this;
        }
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @Override
    public String toString() {
        switch (type()) {
//            case MAP: return arrayValue().toString();
            case STRING: return '"' + stringValue() + '"';
            default: return stringValue();
        }
    }

    // TODO

    public Operand add(Operand operand) {
        throw InterpreterError.binaryApplication("+", type(), operand.type());
    }

    public Operand and(Operand operand) {
        throw InterpreterError.binaryApplication("&", type(), operand.type());
    }

    public Operand or(Operand operand) {
        throw InterpreterError.binaryApplication("|", type(), operand.type());
    }

    public Operand xor(Operand operand) {
        throw InterpreterError.binaryApplication("^", type(), operand.type());
    }

    public Operand shl(Operand operand) {
        throw InterpreterError.binaryApplication("<<", type(), operand.type());
    }

    public Operand shr(Operand operand) {
        throw InterpreterError.binaryApplication(">>", type(), operand.type());
    }

    public Operand sub(Operand operand) {
        throw InterpreterError.binaryApplication("-", type(), operand.type());
    }

    public Operand mul(Operand operand) {
        throw InterpreterError.binaryApplication("*", type(), operand.type());
    }

    public Operand div(Operand operand) {
        throw InterpreterError.binaryApplication("/", type(), operand.type());
    }

    public Operand rem(Operand operand) {
        throw InterpreterError.binaryApplication("%", type(), operand.type());
    }

    public Operand not() {
        throw InterpreterError.unaryApplication("~", type());
    }

    public Operand neg() {
        throw InterpreterError.unaryApplication("-", type());
    }

    public Operand increment() {
        throw InterpreterError.unaryApplication("++", type());
    }

    public Operand decrement() {
        throw InterpreterError.unaryApplication("--", type());
    }

    public void put(Operand key, Operand value) {
        throw InterpreterError.inconvertibleTypes(type(), Type.MAP);
    }

    public Operand get(Operand key) {
        throw InterpreterError.inconvertibleTypes(type(), Type.MAP);
    }

    public void putAll(Operand other) {
        throw InterpreterError.inconvertibleTypes(type(), Type.MAP);
    }

    public Set<Map.Entry<Operand, Operand>> entrySet() {
        throw InterpreterError.inconvertibleTypes(type(), Type.MAP);
    }

    public Operand doClone() {
        return this;
    }

    public int length() {
        throw new InterpreterError("cannot calculate length for " + type().name + " type");
    }


    public abstract void writeToAddress(Address address);
}
