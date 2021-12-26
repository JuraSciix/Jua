package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class IfFalse extends JumpState {

    @Override
    public void print(CodePrinter printer) {
        printer.printName("if_false");
        super.print(printer);
    }

    @Override
    public void run(Environment env) {
        if (!env.popBoolean()) {
            env.setPC(destination);
        } else {
            env.nextPC();
        }
    }
}
