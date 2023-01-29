package jua.interpreter;

import jua.runtime.heap.StringHeap;

/**
 * Утилитарный класс для работы с {@link Address регистрами}.
 */
public class AddressUtils {

    /**
     * Производит аллокацию нового регистра, инициализирует его значением из {@code source} и возвращает.
     */
    public static Address allocateCopy(Address source) {
        Address copy = new Address();
        copy.set(source);
        return copy;
    }

    /**
     * Производит аллокацию массива регистров и возвращает его.
     */
    public static Address[] allocateMemory(int count, int start) {
        if (!(0xFFFF >= count && count >= start && start >= 0)) {
            throw new IllegalArgumentException("count: " + count + ", start: " + start);
        }

        Address[] memory = new Address[count];

        for (int i = start; i < count; i++) {
            memory[i] = new Address();
        }

        return memory;
    }

    /**
     * Копирует данные из исходного участка в памяти в целевой.
     *
     * @param src       Исходный участок памяти.
     * @param srcOffset Смещение, начиная с которого нужно копировать данные в исходном участке.
     * @param dst       Целевой участок памяти.
     * @param dstOffset Смещение, начиная с которого нужно вставлять данные в целевом участке.
     * @param count     Количество регистров, которые нужно перенести.
     */
    public static void arraycopy(Address[] src, int srcOffset, Address[] dst, int dstOffset, int count) {
        if (src == null) {
            throw new IllegalArgumentException("Source memory is null");
        }

        if (dst == null) {
            throw new IllegalArgumentException("Destination memory is null");
        }

        if (srcOffset < 0 || dstOffset < 0 || count < 0 || (srcOffset + count) > src.length || (dstOffset + count) > dst.length) {
            String message = String.format(
                    "Memory (length, offset): source (%d, %d), destination (%d, %d). Count: %d",
                    src.length, srcOffset, dst.length, dstOffset, count
            );
            throw new IllegalArgumentException(message);
        }

        for (int i = 0; i < count; i++) {
            dst[srcOffset + i].set(src[dstOffset + i]);
        }
    }

    public static void assignObject(Address address, Object o) {
        if (o == null) {
            address.setNull();
        } else if (o.getClass() == Boolean.class) {
            address.set((boolean) o);
        } else if (o.getClass() == Long.class) {
            address.set((long) o);
        } else if (o.getClass() == Double.class) {
            address.set((double) o);
        } else if (o.getClass() == String.class) {
            address.set(new StringHeap((String) o));
        } else if (o.getClass() == Address.class) {
            address.set((Address) o);
        } else {
            throw new IllegalArgumentException(o.getClass().getName());
        }
    }

    public static boolean valid(Address a) {
        return (a != null) && a.isValid();
    }

    public static boolean invalid(Address a) {
        return !valid(a);
    }
}
