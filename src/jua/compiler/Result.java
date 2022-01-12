package jua.compiler;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.ProgramFrame;

import jua.interpreter.Program;

public class Result {

    private final CodeData codeData;
    private final Program program;

    public Result(CodeData codeData, Program program) {
        this.codeData = codeData;
        this.program = program;
    }

    public void print() {
        CodePrinter.printConstants(codeData.constants);
        CodePrinter.print(program);
        CodePrinter.printFunctions(codeData.functions);
    }

    public InterpreterRuntime env() {
        InterpreterRuntime env = new InterpreterRuntime(codeData.functions, codeData.constants);
        ProgramFrame build = program.makeFrame();
        env.setProgram(build);
        return env;
    }
}
