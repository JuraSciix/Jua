package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public interface State {

    /**
     * Следующая инструкция.
     */
    int NEXT = 1;

    void print(CodePrinter printer);

    int run(Environment env);
}
