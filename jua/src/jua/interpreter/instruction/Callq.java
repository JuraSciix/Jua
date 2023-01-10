package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.Address;
import jua.interpreter.InterpreterState;

/**
 * Быстрый вызов функции, без сверки числа аргументов.
 */
public final class Callq implements Instruction {

    private final short index;

    private final byte nargs;

    /**
     * @param index Индекс функции.
     * @param nargs Количество аргументов.
     */
    public Callq(short index, byte nargs) {
        this.index = index;
        this.nargs = nargs;
    }

    @Override
    public int stackAdjustment() { return -(nargs & 0xff) + 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("callq");
        printer.printFunctionRef(index & 0xffff);
        printer.print(nargs & 0xff);
    }

    @Override
    public void run(InterpreterState state) {
        Call.prepareThreadCall(state, index, nargs, true, false);
    }
}