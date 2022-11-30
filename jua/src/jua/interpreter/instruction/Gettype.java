package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

/**
 * Снимает операнд со стека и возвращает его тип.
 *
 * <strong>ВНИМАНИЕ: ЭТА ИНСТРУКЦИЯ ВРЕМЕННАЯ</strong>
 */
// todo: Переименовать в Typeof
@Deprecated
public final class Gettype implements Instruction {

    public static final Gettype INSTANCE = new Gettype();

    @Override
    public int stackAdjustment() {
        return -1 + 1;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("gettype");
    }

    @Override
    public void run(InterpreterState state) {
        state.stackGettype();
    }
}
