package jua.runtime.interpreter.instruction;

import jua.runtime.interpreter.ExecutionContext;

public interface Instruction {

    /**
     * Выполняет инструкцию.
     *
     * @param context Контекст выполнения функции.
     */
    void execute(ExecutionContext context);

    int opcode();
}
