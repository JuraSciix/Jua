package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterThread;

public class Getconst implements Instruction {

    private final String name;

    public Getconst(String name) {
        this.name = name;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("getconst");
        printer.print(name);
    }

    @Override
    public int run(InterpreterThread thread) {
        thread.pushStack(thread.getConstantByName(name));
        return NEXT;
    }
}