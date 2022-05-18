package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.compiler.CodePrinter;

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
    public int run(InterpreterThread env) {
        super.run(env);
        System.out.println();
        return NEXT;
    }
}