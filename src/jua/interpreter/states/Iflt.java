package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public final class Iflt extends JumpState {

    private final int value;

    public Iflt(int value) {
        this.value = value;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("iflt");
        printer.print(value);
        super.print(printer);
    }

    @Override
    public int run(Environment env) {
        if (env.popInt() < value) {
            return destination;
        } else {
            return NEXT;
        }
    }
}