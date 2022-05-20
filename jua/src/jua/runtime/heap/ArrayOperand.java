package jua.runtime.heap;

import jua.interpreter.InterpreterError;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ArrayOperand extends Operand {

    // todo: Желательно, чтобы мы использовали свою реализацию LinkedHashMap..

    private final LinkedHashMap<Operand, Operand> map;

    public ArrayOperand() {
        map = new LinkedHashMap<>();
    }

    public ArrayOperand(LinkedHashMap<Operand, Operand> map) {
        this.map = new LinkedHashMap<>(map);
    }

    @Deprecated
    public ArrayOperand(Array array) {
        map = new LinkedHashMap<>(array.map);
    }

    @Override
    public Type type() {
        return Type.MAP;
    }

    @Override
    public boolean isMap() {
        return true;
    }

    @Override
    public boolean canBeMap() { // todo: Зачем этот метод???
        return true;
    }

    @Override
    public boolean canBeBoolean() {
        return true;
    }

    @Override
    public boolean canBeString() {
        return false;
    }

    @Override
    public boolean booleanValue() {
        return map.size() != 0;
    }

    @Override
    public String stringValue() {
        return super.stringValue();
    }

    @Override
    public void put(Operand key, Operand value) {
        if (!key.type().isScalar()) {
            throw InterpreterError.illegalKeyType(key.type());
        }
        map.put(key, value);
    }

    @Override
    public Operand get(Operand key) {
        if (!key.type().isScalar()) {
            throw InterpreterError.illegalKeyType(key.type());
        }
        return map.get(key);
    }

    @Override
    public void putAll(Operand other) {
        for (Map.Entry<Operand, Operand> entry : other.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Operand doClone() {
        return new ArrayOperand(map);
    }

    @Override
    public int length() {
        return map.size();
    }
}
