package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.InterpreterError;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public class IfLessEqual extends JumpState {

    @Override
    public void print(CodePrinter printer) {
        printer.printName("if_le");
        super.print(printer);
    }

    @Override
    public void run(Environment env) {
        Operand rhs = env.popStack();
        Operand lhs = env.popStack();

        if (!lhs.isNumber() || !rhs.isNumber()) {
            // op inverted due to VM mechanics
            throw InterpreterError.binaryApplication(">", lhs.type(), rhs.type());
        }
        if ((lhs.isFloat() || rhs.isFloat())
                ? lhs.floatValue() > rhs.floatValue()
                : lhs.intValue() > rhs.intValue()) {
            env.nextPC();
        } else {
            env.setPC(destination);
        }
    }
}
