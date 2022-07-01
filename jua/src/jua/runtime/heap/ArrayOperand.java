package jua.runtime.heap;

import jua.interpreter.InterpreterError;
import jua.interpreter.InterpreterThread;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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
            InterpreterThread.getInstance().error("Trying to write element to array with non-scalar key");
            return;
        }
        map.put(key, value);
    }

    @Override
    public Operand get(Operand key) {
        if (!key.type().isScalar()) {
            InterpreterThread.getInstance().error("Trying to write element to array with non-scalar key");
            return null;
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
    public Set<Map.Entry<Operand, Operand>> entrySet() {
        return map.entrySet();
    }

    @Override
    public Operand add(Operand operand) {
        if (!operand.isMap()) return super.add(operand);

        //todo: не очень работает сложение [0, 1] + [2, 3] = [2, 3]
        ArrayOperand result = new ArrayOperand();
        result.putAll(this);
        result.putAll(operand);
        return result;
    }

    @Override
    public int length() {
        return map.size();
    }
}
