package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public class Ifge extends JumpState {

    private final long value;

    public Ifge(long value) {
        this.value = value;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ifge");
        printer.printOperand(value);
        super.print(printer);
    }

    @Override
    public int run(Environment env) {
        if (env.popInt() >= value) {
            return destination;
        } else {
            return NEXT;
        }
    }
}