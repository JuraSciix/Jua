package jua.runtime.code;

import java.util.Map;

public final class LocalNameTable {

    private final String[] localNameTable;

    public LocalNameTable(Map<String, Integer> sharedLocalNameTable) {
        // todo: Проверить существование конфликтов переменных в разных областях видимости.
        localNameTable = sharedLocalNameTable.keySet().toArray(new String[0]);
    }

    public String nameOf(int localIndex) {
        return localNameTable[localIndex];
    }
}
