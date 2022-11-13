package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Ifnonnull extends JumpInstruction {

    public Ifnonnull(int destIp) {
        super(destIp);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifnonnull");
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        if (!state.popStack().isNull()) {
            return destIp;
        } else {
            return NEXT;
        }
    }
}