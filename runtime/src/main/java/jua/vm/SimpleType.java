package jua.vm;

public class SimpleType {
    public static final byte TYPE_INVALID = 0;
    public static final byte TYPE_INT64   = 1;
    public static final byte TYPE_FLOAT64 = 2;
    public static final byte TYPE_BOOL64  = 3;
    public static final byte TYPE_PTR64   = 4;

    public static int typeUnionOf(byte lhs, byte rhs) {
        return rhs << 4 | lhs & 0xf;
    }
}
