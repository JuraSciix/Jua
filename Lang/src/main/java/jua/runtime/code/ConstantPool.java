package jua.runtime.code;

import jua.interpreter.Address;
import jua.runtime.Function;

public final class ConstantPool {

    /** Интерфейс для типов, которые могут находиться в пуле констант. */
    public interface Entry {}

    /** Максимальный размер пула констант. */
    public static final int MAX_SIZE = 65535;

    private final Entry[] entries;

    public ConstantPool(Entry[] entries) {
        if (entries == null) {
            throw new IllegalArgumentException("array of entries must not be null");
        }
        if (entries.length > MAX_SIZE) {
            throw new IllegalArgumentException("array of entries must not contain more than ConstantPool.MAX_SIZE elements");
        }
        this.entries = entries;
    }

    public Function getCallee(int index) {
        return (Function) entries[index];
    }

    public Address getAddress(int index) {
        return (Address) entries[index];
    }
}
