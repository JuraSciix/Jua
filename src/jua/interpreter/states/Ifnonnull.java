package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Ifnonnull extends JumpState {

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifnonnull");
        super.print(printer);
    }

    @Override
    public void run(Environment env) {
        if (!env.popStack().isNull()) {
            env.setPC(destination);
        } else {
            env.nextPC();
        }
    }
}
