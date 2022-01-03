package jua.interpreter.opcodes;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.InterpreterError;
import jua.interpreter.runtime.Operand;
import jua.tools.CodePrinter;

public enum Pos implements Opcode {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("pos");
    }

    @Override
    public int run(InterpreterRuntime env) {
        Operand val = env.peekStack();

        if (val.isNull())
            throw InterpreterError.unaryApplication("+", val.type());
        return NEXT;
    }
}