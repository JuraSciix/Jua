package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

import java.util.Arrays;

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
    public int run(InterpreterState state) {
        state.pushStack(state.getConstantByName(name));
        return NEXT;
    }
}