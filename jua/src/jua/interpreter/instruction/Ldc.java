package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Ldc implements Instruction {

    private final int operand;

    public Ldc(int operand) {
        this.operand = operand;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ldc");
        printer.printLiteral(operand);
    }

    @Override
    public int run(InterpreterState state) {
        state.pushStack(state.constant_pool().at(operand));
        return NEXT;
    }
}