package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.LongOperand;

/**
 * Вставляет в стек текущее время UNIX.
 *
 * <strong>ВНИМАНИЕ: ЭТА ИНСТРУКЦИЯ ВРЕМЕННАЯ</strong>
 */
public final class NsTime implements Instruction {

    public static final NsTime INSTANCE = new NsTime();

    @Override
    public int stackAdjustment() { return 1; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ns_time");
    }

    @Override
    public int run(InterpreterState state) {
        state.stackNsTime();
        return NEXT;
    }
}
