package jua.compiler;

public interface Flags {

    // 0x01 = native
    int FN_NATIVE = 0x01;
    // 0x02 = hidden
    // 0x04 = once
    int FN_ONCE = 0x04; // функция выполняется одиножды, затем только возвращает результат.
    int FN_KILLER = 0x04; // функция, которая гарантированно убивает поток. После нее код не выполняется

    static boolean hasFlag(int mask, int flag) {
        return (mask & flag) == flag;
    }
}
