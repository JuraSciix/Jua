package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public interface Opcode {

    /**
     * Следующая инструкция.
     */
    int NEXT = 1;

    void print(CodePrinter printer);

    int run(InterpreterRuntime env);
}
