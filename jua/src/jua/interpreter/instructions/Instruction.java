package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.compiler.CodePrinter;

public interface Instruction {

    /**
     * Следующая инструкция.
     */
    int NEXT = 1;

    int UNREACHABLE = Integer.MIN_VALUE;

    void print(CodePrinter printer);

    int run(InterpreterRuntime env);
}
