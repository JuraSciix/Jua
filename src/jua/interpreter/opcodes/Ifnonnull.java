package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public class Ifnonnull extends ChainOpcode {

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifnonnull");
        super.print(printer);
    }

    @Override
    public int run(InterpreterRuntime env) {
        if (!env.popStack().isNull()) {
            return destination;
        } else {
            return NEXT;
        }
    }
}