package jua.runtime.heap;

import jua.interpreter.Address;
import jua.runtime.ValueType;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public final class MapHeap implements Heap {

    // todo: использовать собственную реализацию карты.

    private final Map<Address, Address> map;

    public MapHeap() {
        map = new HashMap<>();
    }

    public MapHeap(MapHeap original) {
        map = new HashMap<>(original.map);
    }

    public int size() { return map.size(); }

    public boolean isSame(MapHeap that) { return map.equals(that.map); }

    public Heap copy() { return this; }

    public Heap deepCopy() { return new MapHeap(this); }

    public void put(Address key, Address value) {
        ensureScalarKey(key);
        Address storage = map.get(key);
        if (value.isNull()) {
            map.remove(key);
            return;
        }
        if (storage != null) {
            storage.set(value);
        } else {
            map.put(Address.copy(key), Address.copy(value));
        }
    }

    public Address get(Address key) {
        ensureScalarKey(key);
        return map.get(key);
    }

    private void ensureScalarKey(Address key) {
        if (!key.isScalar()) {
            throw new IllegalArgumentException(); // todo
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
            if (value.typeCode() == ValueType.MAP && value.mapValue() == this) {
                esb.append("<self>");
            } else {
                esb.append(value);
            }
            sj.add(esb.toString());
        }
        return sj.toString();
    }
}
