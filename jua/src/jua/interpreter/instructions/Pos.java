package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.InterpreterError;
import jua.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Pos implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("pos");
    }

    @Override
    public int run(InterpreterRuntime env) {
        Operand val = env.peekStack();

        if (!val.isNumber())
            throw InterpreterError.unaryApplication("+", val.type());
        return NEXT;
    }
}