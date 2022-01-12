package jua.interpreter.runtime;

import jua.interpreter.InterpreterError;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public class Array implements Cloneable {

    interface OperandToString {

        String toString(Operand operand);
    }

    public static <K, V> Array fromMap(Map<K, V> map, OperandProducer<K> kc, OperandProducer<V> vc) {
        Array array = new Array();
        map.forEach((key, value) -> array.set(kc.get(key), vc.get(value)));
        return array;
    }

    public static <E> Array fromCollection(Collection<E> collection, OperandProducer<E> conv) {
        Array array = new Array();
        collection.forEach(e -> array.add(conv.get(e)));
        return array;
    }

    public static Array fromArray(int[] values) {
        Array array = new Array();
        for (int value: values) array.add(LongOperand.valueOf(value));
        return array;
    }

    public static Array fromArray(long[] values) {
        Array array = new Array();
        for (long value: values) array.add(LongOperand.valueOf(value));
        return array;
    }

    public static Array fromArray(short[] values) {
        Array array = new Array();
        for (short value: values) array.add(LongOperand.valueOf(value));
        return array;
    }

    public static Array fromArray(byte[] values) {
        Array array = new Array();
        for (byte value: values) array.add(LongOperand.valueOf(value));
        return array;
    }

    public static Array fromArray(boolean[] values) {
        Array array = new Array();
        for (boolean value: values) array.add(BooleanOperand.valueOf(value));
        return array;
    }

    public static Array fromArray(char[] values) {
        Array array = new Array();
        for (char value: values) array.add(StringOperand.valueOf(value + ""));
        return array;
    }

    public static <T> Array fromArray(T[] values, OperandProducer<T> conv) {
        Array array = new Array();
        for (T value: values) array.add(conv.get(value));
        return array;
    }

    private Map<Operand, Operand> map;

    public Array() {
        map = new LinkedHashMap<>();
    }

    public synchronized void add(Operand value) {
        map.put(LongOperand.valueOf(map.size()), value);
    }

    public synchronized void setAll(Array another) {
        if (another == this)
            return;
        map.putAll(another.map);
    }

    public synchronized void set(Operand key, Operand value) {
        key = checkKey(key);

        if (value.isNull()) {
            map.remove(key);
        } else {
            map.put(key, value);
        }
    }

    public synchronized Operand get(Operand key) {
        return map.getOrDefault(checkKey(key), NullOperand.NULL);
    }

    private Operand checkKey(Operand key) {
        Operand.Type type = key.type();

        switch (type) {
            case DOUBLE: return LongOperand.valueOf(key.longValue());
            case BOOLEAN:
            case STRING:
            case LONG: return key;
            default: throw InterpreterError.illegalKeyType(type);
        }
    }

    public int count() {
        return map.size();
    }

    public Array getKeys() {
        Array array = new Array();
        map.keySet().forEach(array::add);
        return array;
    }

    public Array getValues() {
        Array array = new Array();
        map.values().forEach(array::add);
        return array;
    }

    public Operand[] keys() {
        return map.keySet().toArray(new Operand[0]);
    }

    public Operand[] values() {
        return map.values().toArray(new Operand[0]);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Array))
            return false;
        return ((Array) o).map.equals(map);
    }

    @Override
    public int hashCode() {
        int hash = map.hashCode();
        return hash ^ (hash >>> 3);
    }

    @Override
    public Object clone() {
        try {
            Array clone = (Array) super.clone();
            clone.map = new LinkedHashMap<>(map);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @Override
    public String toString() {
        return toString0(Operand::toString);
    }

    String toString0(OperandToString o2s) {
        StringJoiner sj = new StringJoiner(", ", "{", "}");
        map.forEach((key, value) -> sj.add(o2s.toString(key) + ": " + o2s.toString(value)));
        return sj.toString();
    }
}
