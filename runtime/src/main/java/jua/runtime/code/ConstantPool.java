package jua.runtime.code;

import jua.runtime.interpreter.Address;
import jua.runtime.interpreter.AddressSupport;

public final class ConstantPool {

    /** Максимальный размер пула констант. */
    public static final int MAX_SIZE = 65535;

    private final Object[] entries;

    public ConstantPool(Object[] entries) {
        if (entries == null) {
            throw new IllegalArgumentException("array of entries must not be null");
        }
        if (entries.length > MAX_SIZE) {
            throw new IllegalArgumentException("array of entries must not contain more than ConstantPool.MAX_SIZE elements");
        }
        this.entries = entries;
    }

    public ResolvableCallee getCallee(int index) {
        return (ResolvableCallee) entries[index];
    }

    public String getUtf8(int index) {
        return (String) entries[index];
    }

    public void load(int index, Address receiver) {
        AddressSupport.assignObject(receiver, entries[index]);
    }
}
