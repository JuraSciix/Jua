package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Println extends Print {

    public Println(int count) {
        super(count);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("println");
        printer.print(count);
    }

    @Override
    public void run(InterpreterState state) {
        super.run(state);
        if (!state.thread().isError()) {
            System.out.println();
        }
    }
}