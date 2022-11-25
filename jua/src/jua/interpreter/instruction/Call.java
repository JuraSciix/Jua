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
        printer.print(index & 0xffff); // todo: printer.printFunctionRef
        printer.print(nargs & 0xff);
    }

    @Override
    public int run(InterpreterState state) {
        state.set_cp_advance(1);
        state.thread().set_callee(index, nargs);
        return UNREACHABLE; // I'll be back...
    }
}