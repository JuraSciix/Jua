package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Push implements State {

    private final int operand;

    public Push(int operand) {
        this.operand = operand;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("push");
        printer.printLiteral(operand);
    }

    @Override
    public int run(Environment env) {
        env.pushStack(env.getFrame().getConstant(operand));
        return NEXT;
    }
}