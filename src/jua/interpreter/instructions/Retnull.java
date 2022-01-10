package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.Trap;
import jua.interpreter.runtime.NullOperand;
import jua.tools.CodePrinter;

public enum Retnull implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("retnull");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.exitCall(NullOperand.NULL);
        env.getFrame().incPC();
        Trap.bti();
        return 0; // unreachable
    }
}