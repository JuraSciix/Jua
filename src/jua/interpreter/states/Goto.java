package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Goto extends JumpState {

    @Override
    public void print(CodePrinter printer) {
        printer.printName("goto");
        super.print(printer);
    }

    @Override
    public void run(Environment env) {
        env.setPC(destination);
    }
}
