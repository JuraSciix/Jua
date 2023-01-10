package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;
import jua.interpreter.InterpreterThread;

/**
 * Вызов функции без сохранения результата на стеке. Пока что полностью эквивалентно инструкции {@code call}
 */
public final class CallPop implements Instruction {

    private final short index;

    private final byte nargs;

    /**
     * @param index Индекс функции.
     * @param nargs Количество аргументов.
     */
    public CallPop(short index, byte nargs) {
        this.index = index;
        this.nargs = nargs;
    }

    @Override
    public int stackAdjustment() { return -(nargs & 0xff); }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("callpop");
        printer.printFunctionRef(index & 0xffff);
        printer.print(nargs & 0xff);
    }

    @Override
    public void run(InterpreterState state) {
        Call.prepareThreadCall(state, index, nargs, false, true);
    }
}