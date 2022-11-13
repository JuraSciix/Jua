package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.compiler.CodePrinter;

public final class Ifcmple extends JumpInstruction {

    public Ifcmple(int destIp) {
        super(destIp);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifcmple");
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        if (!state.stackCmple()) {
           return NEXT;
        } else {
            return destIp;
        }
    }
}