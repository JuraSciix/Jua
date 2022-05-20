package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;

public final class Switch extends ChainInstruction {

    private final int[] literals;

    private final int[] destIps;

    public Switch(int[] literals, int[] destIps, int defaultIp) {
        super(defaultIp);
        assert literals.length == destIps.length;
        this.literals = literals;
        this.destIps = destIps;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("switch");
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
        printer.printCase(null, destIp /* default ip */);
    }

    @Override
    public int run(InterpreterState state) {
        int[] l = literals;

        Operand selector = state.popStack();

        for (int i = 0; i < l.length; i++) {
            if (selector.equals(state.getConstantPool()[l[i]])) {
                return destIps[i];
            }
        }
        return destIp; /* default ip */
    }
}