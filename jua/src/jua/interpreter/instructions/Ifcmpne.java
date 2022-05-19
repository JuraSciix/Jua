package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.compiler.CodePrinter;

public final class Ifcmpne extends ChainInstruction {

    public Ifcmpne(int destIp) {
        super(destIp);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifcmpne");
        super.print(printer);
    }

    @Override
    public int run(InterpreterRuntime env) {
        if (!env.popStack().equals(env.popStack())) {
            return destIp;
        } else {
            return NEXT;
        }
    }
}