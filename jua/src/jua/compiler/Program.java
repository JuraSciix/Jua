package jua.compiler;

import jua.interpreter.Address;
import jua.interpreter.InterpreterFrame;
import jua.interpreter.InterpreterThread;
import jua.runtime.JuaEnvironment;
import jua.runtime.JuaFunction;
import jua.runtime.code.CodeSegment;
import jua.runtime.heap.Operand;

import java.util.ArrayList;
import java.util.Arrays;

public final class Program {

    public final Source source;

    public final CodeSegment main;

    public final JuaFunction[] functions;

    public final Operand[] constants;

    // Trusting constructor
    Program(Source source, CodeSegment main, JuaFunction[] functions, Operand[] constants) {
        this.source = source;
        this.main = main;
        this.functions = functions;
        this.constants = constants;
    }

    public JuaEnvironment createEnvironment() {
        return new JuaEnvironment(functions, constants);
    }

    public void print() {
        CodePrinter.print(main, 0);
        CodePrinter.printFunctions(new ArrayList<>(Arrays.asList(functions)));
    }

    @Deprecated
    public InterpreterThread toThread() {
        // jua thread
        InterpreterThread j_thread = new InterpreterThread(Thread.currentThread(), createEnvironment());

        // jua function
        JuaFunction main_function = JuaFunction.fromCode("<main>", 0, 0, main, source.name);

        // jua main frame
        InterpreterFrame j_mainFrame = j_thread.makeFrame(main_function);

        j_thread.set_frame_force(j_mainFrame);

        return j_thread;
    }

    public void run() {
        InterpreterThread thread = new InterpreterThread(Thread.currentThread(), createEnvironment());
        JuaFunction functionMain = JuaFunction.fromCode("<main>", 0, 0, main, source.name);
        thread.call(functionMain, new Address[0], null);
    }
}
