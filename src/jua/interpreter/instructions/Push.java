package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.interpreter.runtime.BooleanOperand;
import jua.interpreter.runtime.DoubleOperand;
import jua.interpreter.runtime.LongOperand;
import jua.tools.CodePrinter;

public final class Push implements Instruction {

    public static final int TYPE_INT = 1;
    public static final int TYPE_DOUBLE = 2;
    public static final int TYPE_BOOLEAN = 3;

    public static final Push PUSH_TRUE = new Push(TYPE_BOOLEAN, 1);
    public static final Push PUSH_FALSE = new Push(TYPE_BOOLEAN, 0);

    private final int type;

    private final long value;

    public Push(int type, long value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName((type == TYPE_INT ? 'i' : (type == TYPE_DOUBLE) ? 'f' : 'b') + "push");
        printer.print(value);
    }

    @Override
    public int run(InterpreterRuntime env) {
        switch (type) {
            case TYPE_INT:
                env.pushStack(LongOperand.valueOf(value));
                break;
            case TYPE_DOUBLE:
                env.pushStack(DoubleOperand.valueOf(value));
                break;
            case TYPE_BOOLEAN:
                env.pushStack(BooleanOperand.valueOf(value));
                break;
            default:
                throw new AssertionError();
        }
        return NEXT;
    }
}
