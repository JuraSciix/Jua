package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

import static java.util.Arrays.*;

public class Linearswitch implements Instruction {
    private final int[] literals;
    private final int[] pcCases;
    private final int defaultPC;

    public Linearswitch(int[] literals, int[] pcCases, int defaultPC) {
        this.literals = literals;
        this.pcCases = pcCases;
        this.defaultPC = defaultPC;
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
        printer.printName("linearswitch");
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
        return state.impl_linearswitch(literals, pcCases, defaultPC);
    }
}