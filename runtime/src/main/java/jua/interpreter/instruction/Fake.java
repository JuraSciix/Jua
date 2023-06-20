package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;

/**
 * Это мнимая инструкция, используемая компилятором для резервирования слотов под инструкции.
 * Эта инструкция не предназначена для выполнения и всегда выбрасывает исключение, при попытке
 * отобразить ее или выполнить.
 */
public class Fake implements Instruction {

    private final int stackAdjustment;

    public Fake(int stackAdjustment) {
        this.stackAdjustment = stackAdjustment;
    }

    @Override
    public int stackAdjustment() { return stackAdjustment; }

    @Override
    public void print(InstructionPrinter printer) { throw new AssertionError(this); }

    @Override
    public boolean run(InterpreterState state) { throw new AssertionError(this); }
}
