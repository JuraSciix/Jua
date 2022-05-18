package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.interpreter.Trap;
import jua.compiler.CodePrinter;

/**
 * Данная инструкция прерывает выполнение в текущем фрейме.
 */
public enum Halt implements Instruction {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("halt");
    }

    @Override
    public int run(InterpreterThread env) {
        Trap.halt();
        return NEXT;
    }
}