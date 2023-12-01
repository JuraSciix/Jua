package jua.runtime.interpreter.memory;

import jua.runtime.heap.ListHeap;
import jua.runtime.heap.StringHeap;

public class AddressSupport {

    public static void assignObject(Address address, Object o) {
        if (o == null) {
            address.setNull();
        } else if (o instanceof Boolean) {
            address.set((boolean) o);
        } else if (o instanceof Double) {
            address.set((double) o);
        } else if (o instanceof Float) {
            address.set((float) o);
        } else if (o instanceof Long) {
            address.set((long) o);
        } else if (o instanceof Integer) {
            address.set((int) o);
        } else if (o instanceof Short) {
            address.set((short) o);
        } else if (o instanceof Character) {
            address.set((char) o);
        } else if (o instanceof Byte) {
            address.set((byte) o);
        } else if (o instanceof CharSequence) {
            StringHeap sh
                    = (o instanceof StringHeap)
                    ? ((StringHeap) o).deepCopy()
                    : new StringHeap(o.toString());
            address.set(sh);
        } else if (o instanceof ListHeap) {
            address.set((ListHeap) o);
        } else if (o instanceof Address) {
            address.set((Address) o);
        } else {
            throw new IllegalArgumentException(o.getClass().getName());
        }
    }
}
