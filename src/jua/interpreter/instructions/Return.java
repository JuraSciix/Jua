package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterRuntime;
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
    public int run(InterpreterRuntime env) {
        //todo
        env.exitCall(env.popStack());
        env.getFrame().incPC();
        Trap.bti();
        return 0; // unreachable
    }
}