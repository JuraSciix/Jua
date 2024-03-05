package jua.runtime.memory;

/**
 * <strong>
 *     ЭТО ЧАСТЬ ЭКСПЕРИМЕНТАЛЬНОГО API.
 *     В БУДУЩЕМ ЭТОТ ФАЙЛ И ВСЕ СВЯЗАНЫЕ С НИМ МОГУТ БЫТЬ УДАЛЕНЫ.
 * </strong>
 */
public interface Memory extends ReadOnlyMemory {

    void allocRef(int address);

    void freeRef(int address);

    void setTypeAt(int address, byte type);

    void setLongAt(int address, long value);

    void setDoubleAt(int address, double value);

    void setRefAt(int address, Object value);

    void setNullAt(int address);

    void minimize();
}
