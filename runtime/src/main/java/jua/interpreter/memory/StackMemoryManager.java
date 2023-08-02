package jua.interpreter.memory;

public interface StackMemoryManager {

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
