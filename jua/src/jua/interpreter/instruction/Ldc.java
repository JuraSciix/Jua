package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Ldc implements Instruction {

    private final int operand;

    public Ldc(int operand) {
        this.operand = operand;
    }

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ldc");
        printer.printLiteral(operand);
    }

    @Override
    public int run(InterpreterState state) {
        state.stackLDC(operand);
        return NEXT;
    }
}