package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Ifnull extends JumpState {

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifnull");
        super.print(printer);
    }

    @Override
    public int run(Environment env) {
        if (env.popStack().isNull()) {
            return destination;
        } else {
            return NEXT;
        }
    }
}