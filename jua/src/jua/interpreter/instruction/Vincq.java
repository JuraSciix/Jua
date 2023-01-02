package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public class Vincq implements Instruction {

    private final int id;

    public Vincq(int id) { this.id = id; }

    @Override
    public int stackAdjustment() { return 0; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("vincq");
        printer.printLocal(id);
    }

    @Override
    public void run(InterpreterState state) {
        state.stack_quick_vinc(id);
    }
}