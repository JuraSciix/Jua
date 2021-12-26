package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.InterpreterError;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public class Remainder implements State {

    public static final Remainder REM = new Remainder();

    private Remainder() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("rem");
    }

    @Override
    public void run(Environment env) {
        Operand rhs = env.popStack();
        Operand lhs = env.popStack();

        if (lhs.isNumber() && rhs.isNumber()) {
            if (lhs.isFloat() || rhs.isFloat()) {
                double r = rhs.floatValue();

                if (r == 0D) {
                    throw InterpreterError.divisionByZero();
                }
                env.pushStack(lhs.floatValue() % r);
            } else {
                long r = rhs.intValue();

                if (r == 0L) {
                    throw InterpreterError.divisionByZero();
                }
                env.pushStack(lhs.intValue() % r);
            }
        } else {
            throw InterpreterError.binaryApplication("%", lhs.type(), rhs.type());
        }
        env.nextPC();
    }
}
