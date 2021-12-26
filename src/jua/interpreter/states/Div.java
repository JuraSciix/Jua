package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.InterpreterError;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public class Div implements State {

    public static final Div DIV = new Div();

    private Div() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("div");
    }

    @Override
    public void run(Environment env) {
        Operand rhs = env.popStack();
        Operand lhs = env.popStack();

        if (lhs.isNumber() && rhs.isNumber()) {
            if (lhs.isFloat() || rhs.isFloat()) {
                env.pushStack(lhs.floatValue() / rhs.floatValue());
            } else {
                long l = lhs.intValue();
                long r = rhs.intValue();

                if (r == 0) {
                    throw InterpreterError.divisionByZero();
                }
                if ((l % r) == 0) {
                    env.pushStack(l / r);
                } else {
                    env.pushStack((double) l / r);
                }
            }
        } else {
            throw InterpreterError.binaryApplication("/", lhs.type(), rhs.type());
        }
        env.nextPC();
    }
}
