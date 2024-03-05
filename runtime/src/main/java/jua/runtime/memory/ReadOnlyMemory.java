package jua.runtime.memory;

public interface ReadOnlyMemory {

    byte getTypeAt(int address);

    long getLongAt(int address);

    double getDoubleAt(int address);

    Object getRefAt(int address);
}
