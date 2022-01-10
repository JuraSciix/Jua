package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public interface Instruction {

    /**
     * Следующая инструкция.
     */
    int NEXT = 1;

    void print(CodePrinter printer);

    int run(InterpreterRuntime env);
}
