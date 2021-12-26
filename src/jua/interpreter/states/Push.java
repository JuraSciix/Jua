package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public class Push implements State {

    private final Operand operand;

    public Push(Operand operand) {
        this.operand = operand;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("push_" + operand.type());
        printer.printOperand(operand);
    }

    @Override
    public int run(Environment env) {
        env.pushStack(operand);
        return NEXT;
    }
}