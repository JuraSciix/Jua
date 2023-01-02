package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.Address;
import jua.interpreter.InterpreterState;

public final class Linearswitch extends JumpInstruction {

    private final int[] literals;

    private final int[] destIps;

    public Linearswitch(int[] literals, int[] destIps, int defaultIp) {
        super(defaultIp);
        assert literals.length == destIps.length;
        this.literals = literals;
        this.destIps = destIps;
    }

    @Override
    public int stackAdjustment() { return -1; }

    @Override
    public JumpInstruction negate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("linearswitch");
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
        int[] l = literals;

        Address selector = state.popStack();

        Address tmp = new Address();
        for (int i = 0; i < l.length; i++) {
            state.constant_pool().at(l[i], tmp);
            if (selector.compareTo(tmp) == 0) {
                state.offset(destIps[i]);
                return;
            }
        }
        state.offset(offset); /* default ip */
    }
}