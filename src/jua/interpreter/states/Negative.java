package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.InterpreterError;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public class Negative implements State {

    public static final Negative NEG = new Negative();

    private Negative() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("neg");
    }

    @Override
    public void run(Environment env) {
        Operand val = env.popStack();

        if (val.isInt()) {
            env.pushStack(-val.intValue());
        } else if (val.isFloat()) {
            env.pushStack(-val.floatValue());
        } else {
            throw InterpreterError.unaryApplication("-", val.type());
        }
        env.nextPC();
    }
}
