package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterThread;
import jua.interpreter.Trap;

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
    public int run(InterpreterThread env) {
        env.getFrame().setReturnValue(env.popStack());
        env.returnFrame();
        Trap.bti();
        return UNREACHABLE;
    }
}