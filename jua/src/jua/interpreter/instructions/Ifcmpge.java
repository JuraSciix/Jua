package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.interpreter.InterpreterError;
import jua.runtime.Operand;
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
    public int run(InterpreterThread env) {
        Operand rhs = env.popStack();
        Operand lhs = env.popStack();

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