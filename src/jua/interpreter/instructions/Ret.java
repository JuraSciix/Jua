package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.Trap;
import jua.compiler.CodePrinter;

public enum Ret implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ret");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.exitCall(env.popStack());
        env.getFrame().incPC();
        Trap.bti();
        return 0; // unreachable
    }
}