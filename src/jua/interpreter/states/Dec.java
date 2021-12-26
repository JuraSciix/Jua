package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.InterpreterError;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public class Dec implements State {

    public static final Dec DEC = new Dec();

    private Dec() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("dec");
    }

    @Override
    public void run(Environment env) {
        Operand val = env.popStack();

        if (val.isInt()) {
            env.pushStack(val.intValue() - 1L);
        } else if (val.isFloat()) {
            env.pushStack(val.floatValue() - 1D);
        } else {
            throw InterpreterError.unaryApplication("--", val.type());
        }
        env.nextPC();
    }
}
