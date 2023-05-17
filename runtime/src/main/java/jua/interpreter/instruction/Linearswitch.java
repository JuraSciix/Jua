package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Linearswitch extends JumpInstruction {

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
    public boolean run(InterpreterState state) {
        return state.impl_linearswitch(literals, destIps, offset);
    }
}