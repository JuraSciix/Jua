package jua.interpreter;

/**
 * Утилитарный класс для работы с {@link Address регистрами}.
 */
public class AddressUtils {

    /**
     * Аллоцирует, инициализирует и возвращает копию переданного регистра.
     */
    public static Address allocateCopy(Address source) {
        Address copy = new Address();
        copy.set(source);
        return copy;
    }

    /**
     * Аллоцирует и возвращает массив регистров в памяти.
     */
    public static Address[] allocateMemory(int count, int start) {
        if (!(0xFFFF >= count && count >= start && start >= 0)) {
            throw new IllegalArgumentException("count: " + count + ", start: " + start);
        }

        Address[] memory = new Address[count];

        for (int i = start; i < count; i++) {
            memory[i] = new Address();
        }

        return memory;
    }

    public static void arraycopy(Address[] src, int srcOffset, Address[] dst, int dstOffset, int count) {
        AddressUtils.arraycopy(src, srcOffset, dst, dstOffset, count, CM_DEFAULT);
    }

    /**
     * Копирует данные из исходного участка в памяти в целевой.
     *
     * @param src       Исходный участок памяти.
     * @param srcOffset Смещение, начиная с которого нужно копировать данные в исходном участке.
     * @param dst       Целевой участок памяти.
     * @param dstOffset Смещение, начиная с которого нужно вставлять данные в целевом участке.
     * @param count     Количество регистров, которые нужно перенести.
     * @param copyMode  Режим копирования.
     */
    public static void arraycopy(Address[] src, int srcOffset, Address[] dst, int dstOffset, int count, int copyMode) {
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

        switch (copyMode) {
            case CM_QUICK_SET:
                for (int i = 0; i < count; i++) {
                    dst[srcOffset + i].quickSet(src[dstOffset + i]);
                }
                break;
            case CM_DEFAULT:
                for (int i = 0; i < count; i++) {
                    dst[srcOffset + i].set(src[dstOffset + i]);
                }
                break;
            case CM_SLOW_SET:
                for (int i = 0; i < count; i++) {
                    dst[srcOffset + i].slowSet(src[dstOffset + i]);
                }
                break;
            case CM_REFERRAL:
                System.arraycopy(src, dstOffset, dst, srcOffset, count);
                break;
            default:
                throw new IllegalArgumentException("Illegal copy mode: " + copyMode);
        }
    }

    /**
     * Быстрый режим копирования - копировать ссылки.
     */
    public static final int CM_QUICK_SET = 1;
    /**
     * Стандартный режим копироания - копировать скаляры.
     */
    public static final int CM_DEFAULT = 2;
    /**
     * Медленный режим копироания - копировать все значения.
     */
    public static final int CM_SLOW_SET = 3;
    /**
     * Целевой участок не инициализирован, заполнить его ссылками на регистры из исходного участка.
     */
    public static final int CM_REFERRAL = 4;
}
