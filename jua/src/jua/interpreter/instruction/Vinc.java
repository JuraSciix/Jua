package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public class Vinc implements Instruction {

    private final int id;

    public Vinc(int id) { this.id = id; }

    @Override
    public int stackAdjustment() { return 0; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("vinc");
        printer.printLocal(id);
    }

    @Override
    public int run(InterpreterState state) {
        state.stackVInc(id);
        return NEXT;
    }
}