package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.interpreter.lang.BooleanOperand;
import jua.interpreter.lang.FloatOperand;
import jua.interpreter.lang.IntOperand;
import jua.tools.CodePrinter;

public final class Push implements State {

    public static final int TYPE_INT = 1;
    public static final int TYPE_DOUBLE = 2;
    public static final int TYPE_BOOLEAN = 3;

    public static final Push PUSH_TRUE = new Push(TYPE_INT, 1);
    public static final Push PUSH_FALSE = new Push(TYPE_INT, 0);

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
    public int run(Environment env) {
        switch (type) {
            case TYPE_INT:
                env.pushStack(IntOperand.valueOf(value));
                break;
            case TYPE_DOUBLE:
                env.pushStack(FloatOperand.valueOf(value));
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
