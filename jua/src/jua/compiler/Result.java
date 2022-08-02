package jua.compiler;

import jua.interpreter.InterpreterThread;
import jua.interpreter.InterpreterFrame;

import jua.runtime.JuaEnvironment;
import jua.runtime.code.CodeSegment;
import jua.runtime.JuaFunction;

import java.net.URL;

public class Result {

    private final CodeData codeData;
    private final CodeSegment main;
    private final URL location;

    // Trusting constructor.
    Result(CodeData codeData, CodeSegment main, URL location) {
        this.codeData = codeData;
        this.main = main;
        this.location = location;
    }

    public void print() {
        // todo: закоментировал printConstants
        //CodePrinter.printConstants(codeData.constants);
        CodePrinter.print(main, 0);
        CodePrinter.printFunctions(codeData.functions);
    }

    public JuaEnvironment toEnvironment() {
        return new JuaEnvironment(codeData.functions, codeData.constants);
    }

    public InterpreterThread toThread() {
        // jua thread
        InterpreterThread j_thread = new InterpreterThread(Thread.currentThread(), toEnvironment());

        // jua function
        JuaFunction main_function = JuaFunction.fromCode(null, 0, 0, main, location);

        // jua main frame
        InterpreterFrame j_mainFrame = j_thread.makeFrame(main_function);

        j_thread.set_frame_force(j_mainFrame);

        return j_thread;
    }
}
