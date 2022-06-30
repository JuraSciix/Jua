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
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        if (!lhs.isNumber() || !rhs.isNumber()) {
            // op inverted due to VM mechanics
            throw InterpreterError.binaryApplication("<", lhs.type(), rhs.type());
        }
        if ((lhs.isDouble() || rhs.isDouble())
                ? lhs.doubleValue() < rhs.doubleValue()
                : lhs.longValue() < rhs.longValue()) {
            return NEXT;
        } else {
            return destIp;
        }
    }
}