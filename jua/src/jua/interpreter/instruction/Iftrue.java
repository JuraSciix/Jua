package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Iftrue extends ChainInstruction {

    public Iftrue(int destIp) {
        super(destIp);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("iftrue");
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        return state.popStack().booleanValue() ? destIp : NEXT;
    }
}
