package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Ifcmpne extends JumpInstruction {

    public Ifcmpne(int destIp) {
        super(destIp);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifcmpne");
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        if (!state.stackCmpeq()) {
            return destIp;
        } else {
            return NEXT;
        }
    }
}