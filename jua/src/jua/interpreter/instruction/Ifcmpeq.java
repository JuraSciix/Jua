package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Ifcmpeq extends JumpInstruction {

    public Ifcmpeq(int destIp) {
        super(destIp);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifcmpeq");
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        if (state.stackCmpeq()) {
            return destIp;
        } else {
            return NEXT;
        }
    }
}