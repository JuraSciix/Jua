package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Call implements Instruction {

    private final short index;

    private final byte nargs;

    /**
     * @param index Индекс функции.
     * @param nargs Количество аргументов.
     */
    public Call(short index, byte nargs) {
        this.index = index;
        this.nargs = nargs;
    }

    @Override
    public int stackAdjustment() { return -(nargs & 0xff) + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("call");
        printer.printFunctionRef(index & 0xffff);
        printer.print(nargs & 0xff);
    }

    @Override
    public void run(InterpreterState state) {
        state.impl_call(index & 0xffff, nargs & 0xff);
    }

}