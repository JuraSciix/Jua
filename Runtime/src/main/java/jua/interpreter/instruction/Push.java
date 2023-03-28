package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Push implements Instruction {

    private final int constantIndex;

    public Push(int constantIndex) {
        this.constantIndex = constantIndex;
    }

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("push");
        printer.printLiteral(constantIndex);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackPush(constantIndex);
    }
}