package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public final class Ifcmpne extends ChainInstruction {

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifcmpne");
        super.print(printer);
    }

    @Override
    public int run(InterpreterRuntime env) {
        if (!env.popStack().equals(env.popStack())) {
            return destination;
        } else {
            return NEXT;
        }
    }
}