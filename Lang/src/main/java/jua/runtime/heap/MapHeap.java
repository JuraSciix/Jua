package jua.runtime.heap;

import jua.interpreter.Address;
import jua.interpreter.AddressUtils;
import jua.interpreter.InterpreterThread;
import jua.runtime.ValueType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringJoiner;

public final class MapHeap extends Heap {

    // todo: использовать собственную реализацию карты.

    private final Map<Address, Address> map;

    public MapHeap() {
        map = new HashMap<>();
    }

    public MapHeap(MapHeap original) {
        map = new HashMap<>(original.map);
    }

    public int size() { return map.size(); }

    public boolean isEmpty() { return size() == 0; }

    public boolean nonEmpty() { return !isEmpty(); }

    public boolean isSame(MapHeap that) { return map.equals(that.map); }

    public MapHeap refCopy() { return this; }

    public MapHeap deepCopy() { return new MapHeap(this); }

    public int compare(MapHeap that, int except) {
        Map<Address, Address> m1 = map;
        Map<Address, Address> m2 = that.map;
        Iterator<Map.Entry<Address, Address>> i1 = m1.entrySet().iterator();
        Iterator<Map.Entry<Address, Address>> i2 = m2.entrySet().iterator();
        while (i1.hasNext() && i2.hasNext()) {
            Map.Entry<Address, Address> e1 = i1.next();
            Map.Entry<Address, Address> e2 = i2.next();
            int cmp1 = e1.getKey().weakCompare(e2.getKey(), except);
            if (cmp1 != 0) return cmp1;
            int cmp2 = e1.getValue().weakCompare(e2.getValue(), except);
            if (cmp2 != 0) return cmp2;
        }
        return m1.size() - m2.size();
    }

    public void put(Address key, Address value) {
        ensureScalarKey(key);
        if (key.isNull()) {
            map.remove(key);
            return;
        }
        Address storage = map.get(key);
        if (storage != null) {
            storage.set(value);
        } else {
            map.put(AddressUtils.allocateCopy(key), AddressUtils.allocateCopy(value));
        }
    }

    public Address get(Address key) {
        ensureScalarKey(key);
        return map.get(key);
    }

    public boolean getTo(Address key, Address consumer) {
        Address value = map.get(key);
        if (value == null) {
            consumer.setNull();
            return false;
        }
        consumer.set(value);
        return true;
    }

    private void ensureScalarKey(Address key) {
        if (!key.isScalar()) {
            throw new IllegalArgumentException(); // todo
        }
    }

    public void push(Address value) {
        Address key = new Address();
        key.set(map.size());
        map.put(key, AddressUtils.allocateCopy(value));
    }

    public MapHeap keys() {
        MapHeap keys = new MapHeap();
        for (Address key : map.keySet()) {
            keys.push(key);
        }
        return keys;
    }

    public MapHeap values() {
        MapHeap keys = new MapHeap();
        for (Address key : map.values()) {
            keys.push(key);
        }
        return keys;
    }

    public boolean containsKey(Address key) {
        return map.containsKey(key);
    }

    public void ensureKeyPresent(Address key) {
        if (!map.containsKey(key)) {
            InterpreterThread.threadError("access to undefined element");
        }
    }

    @Override
    public int hashCode() { return map.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return isSame((MapHeap) o);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("{", ", ", "}");
        for (Map.Entry<Address, Address> entry : map.entrySet()) {
            StringBuilder esb = new StringBuilder();
            esb.append(entry.getKey().toString());
            esb.append(": ");
            Address value = entry.getValue();
            if (value.hasType(ValueType.MAP) && value.getMapHeap() == this) {
                esb.append("<self>");
            } else {
                esb.append(value);
            }
            sj.add(esb.toString());
        }
        return sj.toString();
    }
}
