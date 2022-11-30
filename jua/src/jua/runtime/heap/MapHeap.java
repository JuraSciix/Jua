package jua.runtime.heap;

import jua.interpreter.Address;
import jua.runtime.ValueType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringJoiner;

public final class MapHeap implements Heap {

    private static final ThreadLocal<MapHeap> TEMP = new ThreadLocal<MapHeap>() {
        @Override
        protected MapHeap initialValue() {
            return new MapHeap();
        }
    };

    public static MapHeap temp() {
        return TEMP.get();
    }

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

    public MapHeap copy() { return this; }

    public MapHeap deepCopy() { return new MapHeap(this); }

    public int compare(MapHeap that, int except) {
        Map<Address, Address> m1 = map;
        Map<Address, Address> m2 = that.map;
        Iterator<Map.Entry<Address, Address>> i1 = m1.entrySet().iterator();
        Iterator<Map.Entry<Address, Address>> i2 = m2.entrySet().iterator();
        while (i1.hasNext() && i2.hasNext()) {
            Map.Entry<Address, Address> e1 = i1.next();
            Map.Entry<Address, Address> e2 = i2.next();
            int cmp1 = e1.getKey().quickCompare(e2.getKey(), except);
            if (cmp1 != 0) return cmp1;
            int cmp2 = e1.getValue().quickCompare(e2.getValue(), except);
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
            map.put(Address.allocateCopy(key), Address.allocateCopy(value));
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
