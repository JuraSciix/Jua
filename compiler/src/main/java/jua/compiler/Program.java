package jua.compiler;

import jua.interpreter.Address;
import jua.interpreter.InterpreterThread;
import jua.runtime.Function;
import jua.runtime.JuaEnvironment;

import java.util.ArrayList;
import java.util.Arrays;

public final class Program {

    public final Source source;

    public final Function main;

    public final Function[] functions;

    public final Address[] constants;

    // Trusting constructor
    Program(Source source, Function main, Function[] functions, Address[] constants) {
        this.source = source;
        this.main = main;
        this.functions = functions;
        this.constants = constants;
    }

    public JuaEnvironment createEnvironment() {
        return new JuaEnvironment(functions, constants);
    }

    public void print() {
        CodePrinter.print(this, main.userCode(), 0);
        CodePrinter.printFunctions(this, new ArrayList<>(Arrays.asList(functions)));
    }

    public void run() {
        InterpreterThread thread = new InterpreterThread(Thread.currentThread(), createEnvironment());
        thread.callAndWait(main, new Address[0], thread.getTempAddress());
    }
}
