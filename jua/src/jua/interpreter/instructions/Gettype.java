package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterThread;
import jua.runtime.StringOperand;

/**
 * Снимает операнд со стека и возвращает его тип.
 *
 * <strong>ВНИМАНИЕ: ЭТА ИНСТРУКЦИЯ ВРЕМЕННАЯ</strong>
 */
public enum Gettype implements Instruction {

    INSTANCE;


    @Override
    public void print(CodePrinter printer) {
        printer.printName("gettype");
    }

    @Override
    public int run(InterpreterThread thread) {
        thread.pushStack(new StringOperand(thread.popStack().type().name));
        return NEXT;
    }
}
