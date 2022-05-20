package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.LongOperand;

/**
 * Вставляет в стек текущее время UNIX.
 *
 * <strong>ВНИМАНИЕ: ЭТА ИНСТРУКЦИЯ ВРЕМЕННАЯ</strong>
 */
public enum NsTime implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("ns_time");
    }

    @Override
    public int run(InterpreterState state) {
        state.pushStack(new LongOperand(System.nanoTime()));
        return NEXT;
    }
}
