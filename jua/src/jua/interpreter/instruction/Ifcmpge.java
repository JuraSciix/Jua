package jua.interpreter.instruction;

import jua.interpreter.InterpreterError;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public final class Ifcmpge extends ChainInstruction {

    public Ifcmpge(int destIp) {
        super(destIp);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifcmpge");
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        if (!state.stackCmpge()) {
            return NEXT;
        } else {
            return destIp;
        }
    }
}