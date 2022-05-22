package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Call implements Instruction {

    private final int id;
    private final int argc;

    public Call(int id, int argc) {
        this.id = id;
        this.argc = argc;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("call");
        printer.print(id);
        printer.print(argc);
    }

    @Override
    public int run(InterpreterState state) {
        state.set_cp_advance(1);
        if (state.thread().environment().getFunction(id) == null) {
            // Функции не существует.
            state.thread().error("calling an undefined function '" + id + "'");
            return ERROR;
        }
        state.thread().set_callee(id, argc);
        return 0; // I'l be back...
    }
}