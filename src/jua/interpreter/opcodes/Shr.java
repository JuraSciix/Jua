package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.InterpreterError;
import jua.interpreter.runtime.Operand;
import jua.tools.CodePrinter;

public enum Shr implements Opcode {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("shr");
    }

    @Override
    public int run(InterpreterRuntime env) {
        Operand rhs = env.popStack();
        Operand lhs = env.popStack();

        if (lhs.isInt() && rhs.isInt()) {
            env.pushStack(lhs.intValue() >> rhs.intValue());
        } else {
            throw InterpreterError.binaryApplication(">>", lhs.type(), rhs.type());
        }
        return NEXT;
    }
}