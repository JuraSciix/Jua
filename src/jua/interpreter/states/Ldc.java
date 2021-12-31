package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public final class Ldc implements State {

    private final int operand;

    public Ldc(int operand) {
        this.operand = operand;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ldc");
        printer.printLiteral(operand);
    }

    @Override
    public int run(Environment env) {
        env.pushStack(env.getFrame().getConstant(operand));
        return NEXT;
    }
}