package jua.runtime.code;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import jua.runtime.heap.DoubleOperand;
import jua.runtime.heap.LongOperand;
import jua.runtime.heap.Operand;
import jua.runtime.heap.StringOperand;

import java.util.HashMap;

public final class ConstantPool {

    private static class IndexedOperand {

        final int index;

        final Operand operand;

        IndexedOperand(int index, Operand operand) {
            this.index = index;
            this.operand = operand;
        }
    }

    public static final class Builder {

        private final HashMap<Object, IndexedOperand> entries = new HashMap<>();

        private final IntArrayList defaultLocalEntries = new IntArrayList();

        private int defaultLocalEntriesOffset;

        private int next_index() {           return entries.size(); }
        private int index_of(Object value) { return entries.get(value).index; }

        public int putLongEntry(long value) {
            if (entries.containsKey(value)) {
                return index_of(value);
            }

            return putEntry(value, LongOperand.valueOf(value));
        }

        public int putDoubleEntry(double value) {
            if (entries.containsKey(value)) {
                return index_of(value);
            }

            return putEntry(value, DoubleOperand.valueOf(value));
        }

        public int putStringEntry(String value) {
            if (entries.containsKey(value)) {
                return index_of(value);
            }

            return putEntry(value, StringOperand.valueOf(value));
        }

        private int putEntry(Object value, Operand operand) {
            int next_idx = next_index();
            entries.put(value, new IndexedOperand(next_idx, operand));
            return next_idx;
        }

        public void putDefaultLocalEntry(int local_idx, int entry_idx) {
            if (defaultLocalEntries.isEmpty()) {
                defaultLocalEntriesOffset = local_idx;
            }
            defaultLocalEntries.add(entry_idx);
        }

        public ConstantPool build() {
            Operand[] _entries = new Operand[entries.size()];
            for (IndexedOperand ow : entries.values()) {
                _entries[ow.index] = ow.operand;
            }
            return new ConstantPool(_entries, defaultLocalEntries.toIntArray(), defaultLocalEntriesOffset);
        }
    }

    private final Operand[] entries;

    private final int[] default_local_entries;

    private final int default_local_entries_offset;

    ConstantPool(Operand[] entries, int[] default_local_entries, int default_local_entries_offset) {
        this.entries = entries;
        this.default_local_entries = default_local_entries;
        this.default_local_entries_offset = default_local_entries_offset;
    }

    public int size() {
        return entries.length;
    }

    public Operand at(int index) {
        return entries[index];
    }

    public Operand defaultLocalAt(int local_idx) {
        int entry_idx = default_local_entries[local_idx - default_local_entries_offset];
        return entries[entry_idx];
    }
}
