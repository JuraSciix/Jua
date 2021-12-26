package jua.compiler;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

import static jua.interpreter.Program.Builder;

public class Result {

    private final BuiltIn builtIn;
    private final Builder builder;

    public Result(BuiltIn builtIn, Builder builder) {
        this.builtIn = builtIn;
        this.builder = builder;
    }

    public void print() {
        CodePrinter.printConstants(builtIn.constants);
        CodePrinter.print(builder);
        CodePrinter.printFunctions(builtIn.functions);
    }

    public Environment env() {
        Environment env = new Environment(builtIn.functions, builtIn.constants);
        env.setProgram(builder.build());
        return env;
    }
}
