package jua.compiler;

import jua.interpreter.address.Address;
import jua.interpreter.InterpreterThread;
import jua.runtime.ConstantMemory;
import jua.runtime.Function;
import jua.runtime.JuaEnvironment;

public final class Module {

    public final Source source;

    public final Function main;

    public final Function[] functions;

    public final ConstantMemory[] constants;

    // Trusting constructor
    Module(Source source, Function main, Function[] functions, ConstantMemory[] constants) {
        this.source = source;
        this.main = main;
        this.functions = functions;
        this.constants = constants;
    }

    public JuaEnvironment createEnvironment() {
        return new JuaEnvironment(functions, constants);
    }

    public void print() {
        InstructionPrinterImpl.printModule(this);
    }

    public void run() {
        InterpreterThread thread = new InterpreterThread(Thread.currentThread(), createEnvironment());
        thread.callAndWait(main, new Address[0], thread.getTempAddress());
    }
}
