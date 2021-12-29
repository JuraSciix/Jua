package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Ifne extends JumpState {

    private final long value;

    public Ifne(long value) {
        this.value = value;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifne");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public int run(Environment env) {
        if (env.popInt() == value) {
            return destination;
        } else {
            return NEXT;
        }
    }
}