package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.InterpreterError;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public class Shr implements State {

    public static final Shr RSH = new Shr();

    private Shr() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("rsh");
    }

    @Override
    public void run(Environment env) {
        Operand rhs = env.popStack();
        Operand lhs = env.popStack();

        if (lhs.isInt() && rhs.isInt()) {
            env.pushStack(lhs.intValue() >> rhs.intValue());
        } else {
            throw InterpreterError.binaryApplication(">>", lhs.type(), rhs.type());
        }
        env.nextPC();
    }
}
