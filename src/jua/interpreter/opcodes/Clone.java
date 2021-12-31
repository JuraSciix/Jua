package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.runtime.Operand;
import jua.tools.CodePrinter;

public enum Clone implements Opcode {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("clone");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.pushStack((Operand) env.popStack().clone());
        return NEXT;
    }
}