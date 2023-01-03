package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public interface Instruction {

    /**
     * Следующая инструкция.
     */
    int NEXT = 1;

    int ERROR = Integer.MIN_VALUE;

    int UNREACHABLE = Integer.MIN_VALUE;

    int stackAdjustment();

    void print(CodePrinter printer);

    void run(InterpreterState state);
}
