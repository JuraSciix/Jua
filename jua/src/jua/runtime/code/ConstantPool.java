package jua.runtime.code;

import jua.interpreter.Address;

public final class ConstantPool {

    /** Максимальный размер пула констант. */
    public static final int MAX_SIZE = 65535;

    private final Address[] entries;

    public ConstantPool(Address[] entries) {
        this.entries = entries;
    }

    /**
     * Копирует значение константы в адрес.
     *
     * @param index    Индекс константы.
     * @param consumer Адрес, в который будет скопировано значение константы.
     */
    public void load(int index, Address consumer) {
        if (index < 0 || index > entries.length) {
            throw new IndexOutOfBoundsException(Integer.toString(index));
        }
        consumer.slowSet(entries[index]);
    }
}
