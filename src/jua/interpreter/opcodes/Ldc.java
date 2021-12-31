package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public final class Ldc implements Opcode {

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
    public int run(InterpreterRuntime env) {
        env.pushStack(env.getFrame().getConstant(operand));
        return NEXT;
    }
}