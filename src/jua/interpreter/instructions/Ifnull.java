package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.compiler.CodePrinter;

public class Ifnull extends ChainInstruction {

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifnull");
        super.print(printer);
    }

    @Override
    public int run(InterpreterRuntime env) {
        if (env.popStack().isNull()) {
            return destination;
        } else {
            return NEXT;
        }
    }
}