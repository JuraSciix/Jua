package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

public class Call implements Instruction {

    private final int index;

    private final int argc;

    public Call(int index, int argc) {
        this.index = index;
        this.argc = argc;
    }

    @Override
    public int stackAdjustment() { return -argc + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("call");
        printer.printFunctionRef(index);
        printer.print(argc);
    }

    @Override
    public boolean run(InterpreterState state) {
        return state.impl_call(index, argc);
    }

}