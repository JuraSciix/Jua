package jua.interpreter.instructions;

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
    public int run(InterpreterState state) {
        super.run(state);
        System.out.println();
        return NEXT;
    }
}