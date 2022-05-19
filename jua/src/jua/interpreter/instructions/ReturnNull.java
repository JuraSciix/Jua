package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterThread;
import jua.interpreter.Trap;
import jua.runtime.NullOperand;

public final class ReturnNull implements Instruction {

    public static final ReturnNull INSTANCE = new ReturnNull();

    private ReturnNull() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("return_null");
    }

    @Override
    public int run(InterpreterThread thread) {
        thread.getFrame().setReturnValue(NullOperand.NULL);
        thread.returnFrame();
        Trap.bti();
        return UNREACHABLE;
    }
}