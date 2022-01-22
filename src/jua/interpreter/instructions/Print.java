package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.compiler.CodePrinter;

public class Print implements Instruction {

    protected final int count;

    public Print(int count) {
        this.count = count;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("print");
        printer.print(count);
    }

    @Override
    public int run(InterpreterRuntime env) {
        for (int i = 0; i < count; i++) {
            System.out.print(env.popString());
        }
        return NEXT;
    }
}