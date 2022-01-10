package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public class Goto extends ChainInstruction {

    @Override
    public void print(CodePrinter printer) {
        printer.printName("goto");
        super.print(printer);
    }

    @Override
    public int run(InterpreterRuntime env) {
        return destination;
    }
}