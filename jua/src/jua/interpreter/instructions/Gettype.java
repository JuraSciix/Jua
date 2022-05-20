package jua.interpreter.instructions;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;
import jua.runtime.heap.StringOperand;

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
    public int run(InterpreterState state) {
        state.pushStack(new StringOperand(state.popStack().type().name));
        return NEXT;
    }
}
