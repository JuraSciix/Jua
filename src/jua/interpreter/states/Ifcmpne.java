package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Ifcmpne extends JumpState {

    @Override
    public void print(CodePrinter printer) {
        printer.printName("if_ne");
        super.print(printer);
    }

    @Override
    public void run(Environment env) {
        if (!env.popStack().equals(env.popStack())) {
            env.setPC(destination);
        } else {
            env.nextPC();
        }
    }
}
