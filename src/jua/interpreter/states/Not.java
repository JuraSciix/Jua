package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.InterpreterError;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public enum Not implements State {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("not");
    }

    @Override
    public int run(Environment env) {
        Operand val = env.popStack();

        if (val.isInt()) {
            env.pushStack(~val.intValue());
        } else {
            throw InterpreterError.unaryApplication("~", val.type());
        }
        return NEXT;
    }
}