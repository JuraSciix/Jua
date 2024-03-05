package jua.runtime.memory;

import jua.runtime.heap.Heap;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;

import static jua.runtime.Types.T_NULL;

/**
 * Реализация арены памяти на основе буферов.
 * Сравнение производительностью реализации на буферах с аналогом без буферов:
 * <ul>
 *     <li>Ссылки: медленнее на 30%</li>
 *     <li>Целые числа: одинаково</li>
 *     <li>Вещественные числа: медленнее на 20%</li>
 * </ul>
 */
public class BufferMemory implements Memory {

    private ByteBuffer typeBuf;
    private LongBuffer buffer;
    private int bufferTop = 0;
    // Длины массив heap и heapAddrs ОБЯЗАНЫ всегда совпадать.
    private Object[] heap;
    private int[] heapAddrs;
    private int heapTop = 0;
    private boolean heapDirty = false;

    public BufferMemory(int capacity, int heapCapacity) {
        typeBuf = ByteBuffer.allocate(capacity);
        buffer = LongBuffer.allocate(capacity);
        heap = new Heap[heapCapacity];
        heapAddrs = new int[heapCapacity];
        Arrays.fill(heapAddrs, -1);
    }

    @Override
    public void allocRef(int address) {
        ensureOneMoreRef();
        int ref = heapTop++;
        buffer.put(address, ref);
        // Пока в куче на этом месте должен быть нуль.
        heapAddrs[ref] = address;
        // Мы записываем значение в конце, поэтому не нарушаем непрерывность кучи,
        // устанавливать heapDirty=true не нужно.
    }

    private void ensureOneMoreRef() {
        boolean shouldGrow = false;
        if (heapTop >= heap.length) {
            if (heapDirty) {
                minimizeImpl();
                // Если были освобождения, то обязательно найдется 1 свободное место.
            } else {
                // Куча сжата, освобождений не было, следовательно, вся куча заполнена доверху.
                shouldGrow = true;
            }
        }
        if (shouldGrow) {
            heap = Arrays.copyOf(heap, heapTop * 2);
            heapAddrs = Arrays.copyOf(heapAddrs, heapTop * 2);
        }
    }

    @Override
    public void freeRef(int address) {
        int ref = (int)buffer.get(address);
        heap[ref] = null;
        heapAddrs[ref] = -1;
        // Имеет ли смысл сравнивать ref с heapTop, чтобы определять непрерывность кучи?
        // Наверное, нет, так как это, скорее всего, будет раритетным случаем.
        heapDirty = true;
    }

    @Override
    public byte getTypeAt(int address) {
        return typeBuf.get(address);
    }

    @Override
    public void setTypeAt(int address, byte type) {
        typeBuf.put(address, type);
    }

    @Override
    public long getLongAt(int address) {
        return buffer.get(address);
    }

    @Override
    public void setLongAt(int address, long value) {
        buffer.put(address, value);
    }

    @Override
    public double getDoubleAt(int address) {
        return Double.longBitsToDouble(buffer.get(address));
    }

    @Override
    public void setDoubleAt(int address, double value) {
        buffer.put(address, Double.doubleToRawLongBits(value));
    }

    @Override
    public Object getRefAt(int address) {
        return heap[(int)buffer.get(address)];
    }

    @Override
    public void setRefAt(int address, Object value) {
        // value может быть нулевым...?
        heap[(int)buffer.get(address)] = value;
    }

    @Override
    public void setNullAt(int address) {
        setTypeAt(address, T_NULL);
    }

    @Override
    public void minimize() {
        // Производим минимизацию только если:
        // 1) В куче была освобождена память
        // 2) смещение от начала кучи больше 80%.
        if (!heapDirty) {
            // Освобождений не было
            return;
        }
        int threshold = 80; /* % */
        if (heapTop * 100 < heap.length * threshold) {
            return;
        }
        minimizeImpl();
        // Если вершина памяти по-прежнему превышает 80%, то пора бы задуматься о расширении...
    }

    private void minimizeImpl() {
        // Тупо считаем в лоб ненулевые значения и перезаписываем их.
        // Можно подумать, что если мы будем группировать ненулевые значения
        // и перемещать группами, то будет быстрее. Как бы не так.
        // Решение в лоб во всех случаях работает или с той же скоростью, или быстрее.
        int k = 0;
        // Все что за пределами heapTop обязано равняться null.
        for (int i = 0; i < heapTop; i++) {
            if (heapAddrs[i] >= 0) {
                heap[k] = heap[i];
                heapAddrs[k] = heapAddrs[i];

                // Обновляем значение ссылки в буфере.
                buffer.put(heapAddrs[i], k);

                k++;
            }
        }
        Arrays.fill(heap, k, heapTop, null);
        heapTop = k;

        // С этого момента считаем, что освобождений не было.
        heapDirty = false;
    }
}
