package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public class Getconst implements Instruction {

    private final int id;

    public Getconst(int id) {
        this.id = id;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("getconst");
        printer.print(id);
    }

    @Override
    public int run(InterpreterState state) {
        state.pushStack(state.getConstantById(id));
        return NEXT;
    }
}