package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public interface State {

    void print(CodePrinter printer);

    void run(Environment env);
}
