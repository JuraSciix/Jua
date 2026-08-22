package jua.vm;

/**
 * Область памяти потока.
 * <p/>
 * В области памяти находится стек и регистры одного потока.
 * Сама область состоит из фреймов, а фреймы состоят из двух частей:
 * <pre>
 *     [ РЕГИСТРЫ ] . . . [ СТЕК ]
 * </pre>
 * <p>
 * Когда происходит вызов, интерпретатор запрашивает новый фрейм, не замораживая неиспользованную память.
 */
public final class ThreadRegion {
    private Address[] data;

    // Frame Offset (<= Stack Pointer).
    // Начало области фрейма.
    // Ниже этого смещения находятся заблокированные данные.
    private int ro = 0;

    // Region Top.
    // Конец области фрейма.
    // Вершина области. Выше этой области данные не инициализированы.
    private int rt = 0;

    // Stack Pointer (< Region Top).
    // Вершина стека в фрейме.
    // Как правило, вершина стека всегда находится над областью регистров.
    private int sp = 0;

    public ThreadRegion() {
        // Отправная точка - это 512 адресов, выделенных на стек и регистровую память.
        data = AddressUtils.allocateMemory(512, 0);
    }

    // Начало области фрейма
    public int offset() { return ro; }

    // Конец области фрейма
    public int top() { return rt; }

    // Указатель на вершину стека.
    public int stackPointer() { return sp; }

    // Возвращает адрес в области текущего фрейма
    public Address registry(int index) {
        if (index < 0) {
            throw new InterpreterException("Negative index");
        }
        int i = ro + index;
        if (i >= rt) {
            throw new InterpreterException(
                    "Index (" + index + ") out of frame's bounds: [" + ro + "; " + rt + ")");
        }
        return data[i];
    }

    public Address stack(int index) {
        int i = sp + index;
        if (rt <= i || i < ro) {
            throw new InterpreterException(
                    "Stack index (" + index + ") out of frame's bounds: [" + ro + "; " + rt + ")");
        }
        return data[i];
    }

    // Инкрементирует указатель на вершину стека.
    public void stackInc() {
        sp++;
    }

    // Декрементирует указатель на вершину стека.
    public void stackDec() {
        sp--;
    }

    // Возвращает адрес на вершине стека.
    public Address stackTop() {
        return data[sp];
    }

    // Очищает текущий фрейм от начала до конца
    public void clear() {
        for (int i = ro; i < rt; i++) {
            data[i].reset();
        }
    }

    // Замораживает данные
    public void block(int registry, int stack) {
        ro = sp;
        rt = ro + registry + stack;
        sp = ro + registry;
        ensureCapacity();
    }

    private void ensureCapacity() {
        if (rt >= data.length) {
            // Ищем оптимальное увеличение, чтобы не отрываться
            // от необходимого в небо, и не скупиться.
            int newLength = data.length + rt / 2;
            // Пока что не ограничиваем размер массива сверху.
            data = AddressUtils.reallocateWithNewLength(data, newLength);
        }
    }

    // Размораживает данные.
    // Требуется указать те же значения, что указывались при заморозке данных.
    public void unblock(int offset, int top, int stackPointer) {
        ro = offset;
        rt = top;
        sp = stackPointer;
    }

    // Проверяет валидность состояния области фрейма.
    public boolean test() {
        return ro <= sp && sp <= rt;
    }
}
