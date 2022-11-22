package jua.interpreter.instruction;

import jua.interpreter.InterpreterState;
import jua.compiler.CodePrinter;

public interface Instruction {

    /**
     * Следующая инструкция.
     */
    int NEXT = 1;

    int ERROR = Integer.MIN_VALUE;

    int UNREACHABLE = Integer.MIN_VALUE;

    int stackAdjustment();

    void print(CodePrinter printer);

    int run(InterpreterState state);
}
