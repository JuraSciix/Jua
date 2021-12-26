package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.InterpreterError;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public class Or implements State {

    public static final Or OR = new Or();

    private Or() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("or");
    }

    @Override
    public int run(Environment env) {
        Operand rhs = env.popStack();
        Operand lhs = env.popStack();

        if (lhs.isInt() && rhs.isInt()) {
            env.pushStack(lhs.intValue() | rhs.intValue());
        } else if (lhs.isBoolean() && rhs.isBoolean()) {
            env.pushStack(lhs.booleanValue() | rhs.booleanValue());
        } else {
            throw InterpreterError.binaryApplication("|", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}