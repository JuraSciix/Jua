package jua.interpreter.instruction;

import jua.interpreter.Address;
import jua.interpreter.InterpreterState;
import jua.runtime.code.ConstantPool;

import static java.util.Arrays.copyOfRange;

public class Binaryswitch implements Instruction {
    private final int[] literals;
    private final int[] pcCases;
    private final int defaultPC;

    public Binaryswitch(int[] literals, int[] pcCases, int defaultPC) {
        assert literals.length == pcCases.length;
        this.literals = literals;
        this.pcCases = pcCases;
        this.defaultPC = defaultPC;
    }

    public void sort(ConstantPool cp) {
        qsort2(cp, literals, pcCases, 0, literals.length - 1);
    }

    static void qsort2(ConstantPool cp, int[] keys, int[] values, int lo, int hi) {
        int i = lo;
        int j = hi;
        int pivot = keys[(i+j)/2];
        Address tmp1 = new Address(), tmp2 = new Address();
        tmp1.set(cp.getAddress(pivot));
        do {
            tmp2.set(cp.getAddress(keys[i]));
            while (tmp1.compareTo(tmp2) > 0) {
                int index = keys[++i];
                tmp2.set(cp.getAddress(index));
            }
            tmp2.set(cp.getAddress(keys[j]));
            while (tmp1.compareTo(tmp2) < 0) {
                int index = keys[--j];
                tmp2.set(cp.getAddress(index));
            }

            if (i <= j) {
                int temp1 = keys[i];
                keys[i] = keys[j];
                keys[j] = temp1;
                int temp2 = values[i];
                values[i] = values[j];
                values[j] = temp2;
                i++;
                j--;
            }
        } while (i <= j);
        if (lo < j) qsort2(cp, keys, values, lo, j);
        if (i < hi) qsort2(cp, keys, values, i, hi);
    }

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void print(InstructionPrinter printer) {
        printer.beginSwitch();
        printer.printName("binaryswitch");
        if (literals.length > 0) {
            int last = pcCases[0];
            int last_index = 0;
            for (int i = 1; i < literals.length; i++) {
                if (pcCases[i] == last) {
                    continue;
                }
                printer.printCase(copyOfRange(literals, last_index, i), pcCases[last_index]);
                last = pcCases[i];
                last_index = i;
            }
            printer.printCase(copyOfRange(literals, last_index, literals.length), pcCases[last_index]);
        }
        printer.printCase(null, defaultPC);
        printer.endSwitch();
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.impl_binaryswitch(literals, pcCases, defaultPC);
    }
}