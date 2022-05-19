package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterFrame;
import jua.interpreter.InterpreterRuntime;
import jua.interpreter.Trap;
import jua.runtime.NullOperand;

public final class Return implements Instruction {

    public static final Return RETURN = new Return();

    private Return() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("return");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.getFrame().setReturnValue(env.popStack());
        env.returnFrame();
        Trap.bti();
        return UNREACHABLE;
    }
}