package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.runtime.NullOperand;
import jua.tools.CodePrinter;

public class PushNull implements Opcode {

    public static final PushNull INSTANCE = new PushNull();

    private PushNull() {
        super();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("push_null");
    }

    @Override
    public int run(InterpreterRuntime env) {
        env.pushStack(NullOperand.NULL);
        return NEXT;
    }
}