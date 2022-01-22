package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.compiler.CodePrinter;

public final class Ifcmpeq extends ChainInstruction {

    public Ifcmpeq(int destIp) {
        super(destIp);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifcmpeq");
        super.print(printer);
    }

    @Override
    public int run(InterpreterRuntime env) {
        if (env.popStack().equals(env.popStack())) {
            return destIp;
        } else {
            return NEXT;
        }
    }
}