package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public class Clone implements State {

    public static final Clone CLONE = new Clone();

    private Clone() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("clone");
    }

    @Override
    public void run(Environment env) {
        env.pushStack((Operand) env.popStack().clone());
        env.nextPC();
    }
}
