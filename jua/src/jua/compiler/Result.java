package jua.compiler;

import jua.interpreter.InterpreterThread;
import jua.interpreter.InterpreterFrame;

import jua.runtime.code.CodeSegment;
import jua.runtime.JuaFunction;

public class Result {

    private final CodeData codeData;
    private final CodeSegment main;

    public Result(CodeData codeData, CodeSegment main) {
        this.codeData = codeData;
        this.main = main;
    }

    public void print() {
        CodePrinter.printConstants(codeData.constants);
        CodePrinter.print(main, 0);
        CodePrinter.printFunctions(codeData.functions);
    }

    public InterpreterThread env() {
        InterpreterThread env = new InterpreterThread(codeData.functions, codeData.constants);
        InterpreterFrame build = InterpreterThread.buildFrame(null,
                new JuaFunction(null, 0, 0, main), main);
        env.setProgram(build);
        return env;
    }
}
