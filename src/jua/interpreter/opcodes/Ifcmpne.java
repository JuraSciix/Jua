package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public final class Ifcmpne extends ChainOpcode {

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