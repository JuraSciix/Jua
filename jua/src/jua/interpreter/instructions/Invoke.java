package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterThread;
import jua.interpreter.Trap;

public final class Invoke implements Instruction {

    // todo: int id
    private final String id;
    private final int argc;

    public Invoke(String id, int argc) {
        this.id = id;
        this.argc = argc;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("invoke");
        printer.print(id);
        printer.print(argc);
    }

    @Override
    public int run(InterpreterThread env) {
        env.getFrame().getState().setAdvancedCP(1);
        env.joinFrame(env.getFunctionByName(id), argc);
        Trap.bti();
        return UNREACHABLE;
    }
}