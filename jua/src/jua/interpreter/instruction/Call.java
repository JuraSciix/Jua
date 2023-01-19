package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.Address;
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
        prepareThreadCall(state, index, nargs, true, true);
    }

    // Accessed for CallPop
    static void prepareThreadCall(InterpreterState state, short index, byte nargs, boolean returnResult, boolean checkArgc) {
        int functionIndex = index & 0xffff;
        int argc = nargs & 0xff;               /* args count */

        Address[] args = new Address[argc];

        int i = argc;
        while (--i >= 0) {
            args[i] = state.popStack();
        }

        Address returnAddress = returnResult ? state.top() : state.thread().getTempAddress();
        state.thread().prepareCall(functionIndex, args, argc, returnAddress, checkArgc);
        state.cleanupStack();
        state.set_cp_advance(1);
    }
}