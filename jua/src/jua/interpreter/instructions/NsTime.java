package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterThread;

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
    public int run(InterpreterThread env) {
        env.pushStack(System.nanoTime());
        return NEXT;
    }
}
