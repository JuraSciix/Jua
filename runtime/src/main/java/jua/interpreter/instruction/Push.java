package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Push implements Instruction {

    private final int constantIndex;

    public Push(int constantIndex) {
        this.constantIndex = constantIndex;
    }

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(InstructionPrinter printer) {
        printer.printName("push");
        printer.printLiteral(constantIndex);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.stackPush(constantIndex);
    }
}