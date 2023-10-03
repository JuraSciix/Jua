package jua.runtime.interpreter.memory;

public interface Memory {

    Address getAddress(int i);

    Memory subRegion(int offset, int count);

    void freeRegion(int offset, int count);

    int size();

    void copyMemory(int srcOffset, Address[] dst, int dstOffset, int count);
}