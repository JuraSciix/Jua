package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

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
    public void run(InterpreterState state) {
        state.stackVInc(id);
    }
}