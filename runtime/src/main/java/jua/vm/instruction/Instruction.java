package jua.vm.instruction;

import jua.vm.ExecutionContext;

public interface Instruction {

    /**
     * Выполняет инструкцию.
     *
     * @param context Контекст выполнения функции.
     */
    void execute(ExecutionContext context);

    int opcode();
}
