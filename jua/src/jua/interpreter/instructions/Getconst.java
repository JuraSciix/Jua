package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterRuntime;

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
    public int run(InterpreterRuntime env) {
        env.pushStack(env.getConstantByName(name));
        return NEXT;
    }
}