package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.InterpreterError;
import jua.interpreter.lang.Operand;
import jua.tools.CodePrinter;

public enum Pos implements State {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("pos");
    }

    @Override
    public void run(Environment env) {
        Operand val = env.peekStack();

        if (val.isNull())
            throw InterpreterError.unaryApplication("+", val.type());
        env.nextPC();
    }
}
