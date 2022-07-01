package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
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
    public int run(InterpreterState state) {
        state.thread().interrupt();
        return 0;
    }
}