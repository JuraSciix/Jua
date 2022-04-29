package jua.compiler;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.InterpreterFrame;

import jua.interpreter.Program;
import jua.runtime.JuaFunction;

public class Result {

    private final CodeData codeData;
    private final Program main;

    public Result(CodeData codeData, Program main) {
        this.codeData = codeData;
        this.main = main;
    }

    public void print() {
        CodePrinter.printConstants(codeData.constants);
        CodePrinter.print(main, 0);
        CodePrinter.printFunctions(codeData.functions);
    }

    public InterpreterRuntime env() {
        InterpreterRuntime env = new InterpreterRuntime(codeData.functions, codeData.constants);
        InterpreterFrame build = InterpreterRuntime.buildFrame(null,
                new JuaFunction(null, 0, 0, main), main);
        env.setProgram(build);
        return env;
    }
}
