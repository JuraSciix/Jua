package jua.runtime.code;

import jua.runtime.LocalTable;

@Deprecated
public final class LocalNameTable {

    private final LocalTable localTable;

    public LocalNameTable(LocalTable localTable) {
        this.localTable = localTable;
    }

    public String nameOf(int localIndex) {
        return localTable.getLocalName(localIndex);
    }

    public int defaultPCIOf(int localIndex) {
        return localTable.getLocalDefaultPCI(localIndex);
    }
}
