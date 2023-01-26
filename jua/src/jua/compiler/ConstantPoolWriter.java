package jua.compiler;

import jua.runtime.code.ConstantPool;
import jua.utils.Pool;

public class ConstantPoolWriter {

    private static final int MAX_SIZE = ConstantPool.MAX_SIZE;

    private final Pool<Object> pool = new Pool<>();

    public int writeLong(long l) { return write(l); }

    public int writeDouble(double d) { return write(d); }

    public int writeString(String str) { return write(str); }

    @Deprecated
    public int writeTrue() { return write(true); }

    @Deprecated
    public int writeFalse() { return write(false); }

    @Deprecated
    public int writeNull() { return write(null); }

    private int write(Object value) {
        checkOverflow();
        return pool.lookup(value);
    }

    private void checkOverflow() {
        if (pool.count() >= MAX_SIZE) {
            throw new CompileException("constant pool cannot contain greater than " + MAX_SIZE + " entries");
        }
    }

    public Object[] toArray() {
        return pool.toArray(new Object[0]);
    }
}
