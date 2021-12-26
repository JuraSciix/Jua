package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Ifnull extends JumpState {

    @Override
    public void print(CodePrinter printer) {
        printer.printName("if_null");
        super.print(printer);
    }

    @Override
    public void run(Environment env) {
        if (env.popStack().isNull()) {
            env.setPC(destination);
        } else {
            env.nextPC();
        }
    }
}
