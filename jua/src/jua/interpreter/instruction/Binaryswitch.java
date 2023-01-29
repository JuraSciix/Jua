package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.Address;
import jua.interpreter.InterpreterState;
import jua.runtime.code.ConstantPool;

public final class Binaryswitch extends JumpInstruction {

    private final int[] literals;

    private final int[] destIps;

    public Binaryswitch(int[] literals, int[] destIps, int defaultIp) {
        super(defaultIp);
        assert literals.length == destIps.length;
        this.literals = literals;
        this.destIps = destIps;
    }

    /**
     *
     */
    public void sort(ConstantPool cp) {
        qsort2(cp, literals, destIps, 0, literals.length - 1);
    }

    static void qsort2(ConstantPool cp, int[] keys, int[] values, int lo, int hi) {
        int i = lo;
        int j = hi;
        int pivot = keys[(i+j)/2];
        Address tmp1 = new Address(), tmp2 = new Address();
        cp.load(pivot, tmp1);
        do {
            cp.load(keys[i], tmp2);
            while (tmp1.compareTo(tmp2) > 0) {
                int index = keys[++i];
                cp.load(index, tmp2);
            }
            cp.load(keys[j], tmp2);
            while (tmp1.compareTo(tmp2) < 0) {
                int index = keys[--j];
                cp.load(index, tmp2);
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
    public JumpInstruction negate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("binaryswitch");
        if (literals.length > 0) {
            int last = destIps[0];
            int last_index = 0;
            for (int i = 1; i < literals.length; i++) {
                if (destIps[i] == last) {
                    continue;
                }
                printer.printCase(java.util.Arrays.copyOfRange(literals, last_index, i), destIps[last_index]);
                last = destIps[i];
                last_index = i;
            }
            printer.printCase(java.util.Arrays.copyOfRange(literals, last_index, literals.length), destIps[last_index]);
        }
        printer.printCase(null, offset /* default ip */);
    }

    @Override
    public void run(InterpreterState state) {
        // Новый, двоичный поиск
        ConstantPool cp = state.constant_pool();

        int l = 0;
        int h = literals.length - 1;

        Address sel = state.popStack();           /* selector */
        Address tmp = new Address(); /* buffer   */

        // Не скалярные значения семантически запрещены
        if (sel.isScalar()) {
            while (l <= h) {
                int i = (l + h) >> 1;
                cp.load(literals[i], tmp);

                int d = sel.compareTo(tmp);

                if (d > 0) {
                    l = i + 1;
                } else if (d < 0) {
                    h = i - 1;
                } else {
                    /* assert d != 2; sel == tmp */
                    state.offset(destIps[i]);
                    return;
                }
            }
        }

        state.offset(offset); /* default offset */
    }
}