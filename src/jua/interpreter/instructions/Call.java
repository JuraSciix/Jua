package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.InterpreterError;
import jua.runtime.RuntimeFunction;
import jua.compiler.CodePrinter;

public class Call implements Instruction {

    private final String name;

    private final byte argc;

    public Call(String name, byte argc) {
        this.name = name;
        this.argc = argc;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("invoke");
        printer.print(name);
        printer.print(argc);
    }

    @Override
    public int run(InterpreterRuntime env) {
        RuntimeFunction fn = env.getFunctionByName(name);

        if (fn == null) {
            throw InterpreterError.functionNotExists(name);
        }
        // todo: Это новый костыль вместо старого..
        env.getFrame().setRunningstate(false);
        fn.call(env, name, argc);
        return NEXT;
    }
}