package jua.runtime.interpreter.memory;

public interface MemoryStack {

    /**
     * Выделяет регион памяти заданного размера.
     *
     * @param count Число участков, которое надо выделить.
     * @return Регион памяти.
     */
    Memory allocate(int count);

    /**
     * Освобождает последние чисто участков.
     *
     * @param count Число участков, которое надо освободить.
     */
    void free(int count);
}
