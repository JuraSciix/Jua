package jua.interpreter.instruction;

import jua.interpreter.InterpreterError;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.Operand;
import jua.compiler.CodePrinter;

public final class Ifcmplt extends ChainInstruction {

    public Ifcmplt(int destIp) {
        super(destIp);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifcmplt");
        super.print(printer);
    }

    @Override
    public int run(InterpreterState state) {
        if (!state.stackCmplt()) {
            return NEXT;
        } else {
            return destIp;
        }
    }
}