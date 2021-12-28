package jua.compiler;

import jua.interpreter.Environment;
import jua.interpreter.Frame;
import jua.tools.CodePrinter;

import jua.interpreter.Program;

public class Result {

    private final BuiltIn builtIn;
    private final Program program;

    public Result(BuiltIn builtIn, Program program) {
        this.builtIn = builtIn;
        this.program = program;
    }

    public void print() {
        CodePrinter.printConstants(builtIn.constants);
        CodePrinter.print(program);
        CodePrinter.printFunctions(builtIn.functions);
    }

    public Environment env() {
        Environment env = new Environment(builtIn.functions, builtIn.constants);
        Frame build = program.build();
        env.setProgram(build);
        return env;
    }
}
