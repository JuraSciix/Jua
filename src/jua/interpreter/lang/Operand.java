package jua.interpreter.lang;

public abstract class Operand {

    public abstract OperandType type();

    public boolean isArray() {
        return false;
    }

    public boolean isBoolean() {
        return false;
    }

    public boolean isFloat() {
        return false;
    }

    public boolean isInt() {
        return false;
    }

    public boolean isNull() {
        return false;
    }

    public boolean isNumber() {
        return false;
    }

    public boolean isString() {
        return false;
    }

    public boolean canBeArray() {
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

    public Array arrayValue() {
        throw new IllegalStateException();
    }

    public boolean booleanValue() {
        throw new IllegalStateException();
    }

    public double floatValue() {
        throw new IllegalStateException();
    }

    public long intValue() {
        throw new IllegalStateException();
    }

    public String stringValue() {
        throw new IllegalStateException();
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
            case ARRAY: return arrayValue().toString();
            case STRING: return '"' + stringValue() + '"';
            default: return stringValue();
        }
    }
}
