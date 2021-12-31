package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.InterpreterError;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public final class Ifcmpgt extends JumpState {

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifcmpgt");
        super.print(printer);
    }

    @Override
    public int run(Environment env) {
        Operand rhs = env.popStack();
        Operand lhs = env.popStack();

        if (!lhs.isNumber() || !rhs.isNumber()) {
            // op inverted due to VM mechanics
            throw InterpreterError.binaryApplication("<=", lhs.type(), rhs.type());
        }
        if ((lhs.isFloat() || rhs.isFloat())
                ? lhs.floatValue() <= rhs.floatValue()
                : lhs.intValue() <= rhs.intValue()) {
            return NEXT;
        } else {
            return destination;
        }
    }
}