package jua.runtime.heap;

import jua.interpreter.Address;
import jua.runtime.ValueType;

import java.util.HashMap;
import java.util.Iterator;
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

    public int compare(MapHeap that, int except) {
        Map<Address, Address> m1 = map;
        Map<Address, Address> m2 = that.map;
        Iterator<Map.Entry<Address, Address>> i1 = m1.entrySet().iterator();
        Iterator<Map.Entry<Address, Address>> i2 = m2.entrySet().iterator();
        while (i1.hasNext() && i2.hasNext()) {
            Map.Entry<Address, Address> e1 = i1.next();
            Map.Entry<Address, Address> e2 = i2.next();
            int cmp1 = e1.getKey().realCompare(e2.getKey(), except);
            if (cmp1 != 0) return cmp1;
            int cmp2 = e1.getValue().realCompare(e2.getValue(), except);
            if (cmp2 != 0) return cmp2;
        }
        return m1.size() - m2.size();
    }

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
