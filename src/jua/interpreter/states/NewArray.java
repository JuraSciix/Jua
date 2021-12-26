package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.ArrayOperand;
import jua.tools.CodePrinter;

public class NewArray implements State {

    public static final NewArray NEWARRAY = new NewArray();

    private NewArray() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("newarray");
    }

    @Override
    public void run(Environment env) {
        env.pushStack(new ArrayOperand());
        env.nextPC();
    }
}
