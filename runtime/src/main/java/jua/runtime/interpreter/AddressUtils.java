package jua.runtime.interpreter;

/**
 * Утилитарный класс для работы с {@link Address регистрами}.
 */
public class AddressUtils {

    /**
     * Производит аллокацию нового регистра, инициализирует его значением из {@code source} и возвращает.
     */
    public static Address allocateCopy(Address source) {
        Address copy = new Address();
        copy.set(source);
        return copy;
    }

    /**
     * Производит аллокацию массива регистров и возвращает его.
     */
    public static Address[] allocateMemory(int count, int start) {
        if (!(count >= start && start >= 0)) {
            throw new IllegalArgumentException("count: " + count + ", start: " + start);
        }

        Address[] memory = new Address[count];

        for (int i = start; i < count; i++) {
            memory[i] = new Address();
        }

        return memory;
    }

    public static Address[] allocateMemoryNulls(int count, int start) {
        if (!(count >= start && start >= 0)) {
            throw new IllegalArgumentException("count: " + count + ", start: " + start);
        }

        Address[] memory = new Address[count];

        for (int i = start; i < count; i++) {
            (memory[i] = new Address()).setNull();
        }

        return memory;
    }

    /**
     * Копирует данные из исходного участка в памяти в целевой.
     *
     * @param src       Исходный участок памяти.
     * @param srcOffset Смещение, начиная с которого нужно копировать данные в исходном участке.
     * @param dst       Целевой участок памяти.
     * @param dstOffset Смещение, начиная с которого нужно вставлять данные в целевом участке.
     * @param count     Количество регистров, которые нужно перенести.
     */
    public static void arraycopy(Address[] src, int srcOffset, Address[] dst, int dstOffset, int count) {
        if (src == null) {
            throw new IllegalArgumentException("Source memory is null");
        }

        if (dst == null) {
            throw new IllegalArgumentException("Destination memory is null");
        }

        if (srcOffset < 0 || dstOffset < 0 || count < 0 || (srcOffset + count) > src.length || (dstOffset + count) > dst.length) {
            String message = String.format(
                    "Memory (length, offset): source (%d, %d), destination (%d, %d). Count: %d",
                    src.length, srcOffset, dst.length, dstOffset, count
            );
            throw new IllegalArgumentException(message);
        }

        for (int i = 0; i < count; i++) {
            dst[srcOffset + i].set(src[dstOffset + i]);
        }
    }

    public static Address[] reallocateWithNewLength(Address[] src, int newLength) {
        Address[] memory = new Address[newLength];
        System.arraycopy(src, 0, memory, 0, Math.min(src.length, newLength));

        for (int i = src.length; i < newLength; i++) {
            memory[i] = new Address();
        }

        return memory;
    }

    public static void fill(Address[] memory, int start, int end, Address value) {
        for (int i = start; i < end; i++) {
            memory[i].set(value);
        }
    }

    public static boolean valid(Address a) {
        return (a != null) && a.isValid();
    }

    public static boolean invalid(Address a) {
        return !valid(a);
    }
}
