package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public interface Instruction {

    /** @return Влияние инструкции на вершину стека. */
    int stackAdjustment();

    /** Печатает информацию об инструкции */
    void print(CodePrinter printer);

    /**
     * Выполняет инструкцию.
     *
     * @param state Состояние фрейма.
     * @return {@code true}, если инструкция успешно выполнена и следует переходить к следующей; {@code false}, если произошла ошибка или необходимо прервать выполнение.
     */
    boolean run(InterpreterState state);
}
