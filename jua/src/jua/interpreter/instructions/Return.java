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
    public int run(InterpreterThread thread) {
        thread.getFrame().setReturnValue(thread.popStack());
        thread.returnFrame();
        Trap.bti();
        return UNREACHABLE;
    }
}