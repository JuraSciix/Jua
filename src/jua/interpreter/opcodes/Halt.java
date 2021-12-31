package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.Trap;
import jua.tools.CodePrinter;

/**
 * Данная инструкция прерывает выполнение в текущем фрейме.
 */
public enum Halt implements Opcode {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("halt");
    }

    @Override
    public int run(InterpreterRuntime env) {
        Trap.halt();
        return NEXT;
    }
}