package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterRuntime;

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
    public int run(InterpreterRuntime env) {
        env.pushStack(System.nanoTime());
        return NEXT;
    }
}
