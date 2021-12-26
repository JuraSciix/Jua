package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.Trap;
import jua.tools.CodePrinter;

/**
 * Данная инструкция прерывает выполнение в текущем фрейме.
 */
public enum Halt implements State {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("halt");
    }

    @Override
    public int run(Environment env) {
        Trap.halt();
        return NEXT;
    }
}