package jua.interpreter.instructions;

import jua.interpreter.InterpreterState;
import jua.runtime.Operand;
import jua.compiler.CodePrinter;

public class Vinc implements Instruction {

    private final int id;

    public Vinc(int id) {
        this.id = id;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("vinc");
        printer.printLocal(id);
    }

    @Override
    public int run(InterpreterState state) {
        Operand local = state.load(id);
        state.store(id, local.increment());
        return NEXT;
    }
}