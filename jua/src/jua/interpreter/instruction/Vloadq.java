package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

/**
 * Quick variable load.
 * Applied to local variables that definitely exists.
 * If we not sure about variable existing, use just {@code vload}
 */
// todo: rename to LoadQuick (vloadq -> loadq)
public final class Vloadq implements Instruction {

    private final int id;

    public Vloadq(int id) {
        this.id = id;
    }

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("vloadq");
        printer.printLocal(id);
    }

    @Override
    public void run(InterpreterState state) {
        state.stack_quick_vload(id);
    }
}