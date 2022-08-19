package jua.compiler;

import jua.interpreter.InterpreterThread;
import jua.interpreter.InterpreterFrame;

import jua.runtime.JuaEnvironment;
import jua.runtime.code.CodeSegment;
import jua.runtime.JuaFunction;
import jua.runtime.heap.Operand;

public class CompileResult {

    // todo: Отрефакторить.

    public final CodeLayout codeLayout;
    public final CodeSegment main;
    public final String filename;

    // Trusting constructor.
    CompileResult(CodeLayout codeLayout, CodeSegment main, String filename) {
        this.codeLayout = codeLayout;
        this.main = main;
        this.filename = filename;
    }

    public void print() {
        // todo: закоментировал printConstants
        //CodePrinter.printConstants(codeData.constants);
        CodePrinter.print(main, 0);
        CodePrinter.printFunctions(codeLayout.functions);
    }

    public JuaEnvironment toEnvironment() {
        return new JuaEnvironment(codeLayout.functions.toArray(new JuaFunction[0]), codeLayout.constants.toArray(new Operand[0]));
    }

    public InterpreterThread toThread() {
        // jua thread
        InterpreterThread j_thread = new InterpreterThread(Thread.currentThread(), toEnvironment());

        // jua function
        JuaFunction main_function = JuaFunction.fromCode(null, 0, 0, main, filename);

        // jua main frame
        InterpreterFrame j_mainFrame = j_thread.makeFrame(main_function);

        j_thread.set_frame_force(j_mainFrame);

        return j_thread;
    }
}
