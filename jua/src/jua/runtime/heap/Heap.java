package jua.runtime.heap;

public interface Heap {

    int size();

    boolean isSame(Heap that);

    Heap copy();

    Heap deepCopy();

    @Override
    int hashCode();

    @Override
    boolean equals(Object o);

    @Override
    String toString();
}
