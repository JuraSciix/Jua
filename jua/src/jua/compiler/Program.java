package jua.compiler;

import jua.interpreter.Address;
import jua.interpreter.InterpreterThread;
import jua.runtime.JuaEnvironment;
import jua.runtime.JuaFunction;

import java.util.ArrayList;
import java.util.Arrays;

public final class Program {

    public final Source source;

    public final JuaFunction main;

    public final JuaFunction[] functions;

    public final Address[] constants;

    // Trusting constructor
    Program(Source source, JuaFunction main, JuaFunction[] functions, Address[] constants) {
        this.source = source;
        this.main = main;
        this.functions = functions;
        this.constants = constants;
    }

    public JuaEnvironment createEnvironment() {
        return new JuaEnvironment(functions, constants);
    }

    public void print() {
        CodePrinter.print(this, main.codeSegment(), 0);
        CodePrinter.printFunctions(this, new ArrayList<>(Arrays.asList(functions)));
    }

    public void run() {
        InterpreterThread thread = new InterpreterThread(Thread.currentThread(), createEnvironment());
        thread.callAndWait(main, new Address[0], null);
    }
}
