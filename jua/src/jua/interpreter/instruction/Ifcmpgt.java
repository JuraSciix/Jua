package jua.interpreter.instruction;

import jua.interpreter.InterpreterError;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public final class Ifcmpgt extends ChainInstruction {

    public Ifcmpgt(int destIp) {
        super(destIp);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifcmpgt");
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        if (!state.stackCmpgt()) {
            return NEXT;
        } else {
            return destIp;
        }
    }
}