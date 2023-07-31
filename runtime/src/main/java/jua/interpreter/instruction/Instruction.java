package jua.interpreter.instruction;

import jua.interpreter.ExecutionContext;

public interface Instruction {

    /**
     * @return Влияние инструкции на вершину стека.
     */
    int stackAdjustment();

    /**
     * Печатает информацию об инструкции
     */
    void print(InstructionPrinter printer);

    default JumpInstruction negated() {
        throw new UnsupportedOperationException(getClass().getName() + " cannot be negated");
    }

    default JumpInstruction withNextCp(int nextCp) {
        throw new UnsupportedOperationException(getClass().getName() + " have no next-cp jump field");
    }

    /**
     * Выполняет инструкцию.
     *
     * @param context Контекст выполнения функции.
     */
    void execute(ExecutionContext context);
}
