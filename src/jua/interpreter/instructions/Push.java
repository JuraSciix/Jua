package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.runtime.BooleanOperand;
import jua.interpreter.runtime.DoubleOperand;
import jua.interpreter.runtime.LongOperand;
import jua.compiler.CodePrinter;
import jua.interpreter.runtime.Operand;

public final class Push implements Instruction {

    public static final Push PUSH_TRUE = new Push(Operand.Type.BOOLEAN, (short) 1);

    public static final Push PUSH_FALSE = new Push(Operand.Type.BOOLEAN, (short) 0);

    private final Operand.Type type;

    private final short value;

    public Push(Operand.Type type, short value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName(type.sigc() + "push");
        printer.print(value);
    }

    @Override
    public int run(InterpreterRuntime env) {
        switch (type) {
            case LONG: env.pushStack(LongOperand.valueOf(value)); break;
            case DOUBLE: env.pushStack(DoubleOperand.valueOf(value)); break;
            case BOOLEAN: env.pushStack(BooleanOperand.valueOf(value)); break;
            default: throw new AssertionError();
        }
        return NEXT;
    }
}