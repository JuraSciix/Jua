package jua.runtime.heap;

public abstract class Heap {

    /** Возвращает новую ссылку на тот же объект. Ничего не делает для примитивов */
    public Heap refCopy() { return this; }

    /** Возвращает новый объект, идентичный оригиналу. С примитивами ничего не делает. */
    public Heap deepCopy() { return this; }
}
