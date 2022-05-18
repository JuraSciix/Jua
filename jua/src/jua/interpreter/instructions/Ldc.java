package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.compiler.CodePrinter;

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
    public int run(InterpreterThread env) {
        env.pushStack(env.getFrame().getConstant(operand));
        return NEXT;
    }
}