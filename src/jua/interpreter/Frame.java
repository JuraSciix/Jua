package jua.interpreter;

import jua.interpreter.lang.Operand;
import jua.interpreter.states.State;

public final class Frame {

    private final Program program;

    private final State[] states;

    private final Operand[] stack;

    private final Operand[] locals;

    private int pc = 0;

    private int tos = 0;

    private boolean runningstate = false;

    Frame(Program program) {
        this.program = program;
        this.states = program.states;
        this.stack = new Operand[program.stackSize];
        this.locals = new Operand[program.localsSize];
    }

    public void incPC() {
        pc++;
    }

    @Deprecated
    public void setPC(int newPC) {
        pc = newPC;
    }

    // todo
    public String sourceName() {
        return program.filename;
    }

    public int currentLine() {
        if (runningstate) {
            throw new IllegalStateException();
        }
        return program.getInstructionLine(pc);
    }

    void run(Environment env) {
        State[] states = this.states;
        int bci = pc;
        runningstate = true;
        try {
            while (true) {
                //System.out.printf("[%d] %s%n", bci, states[bci].getClass().getName());
                bci += states[bci].run(env);
            }
        } finally {
            pc = bci;
            runningstate = false;
        }
    }


    // в двух нижеописанных операциях нет смысла
    public void clearStack() {}

    public void clearLocals() {}

    public void push(Operand operand) {
        stack[tos++] = operand;
    }

    public Operand pop() {
        Operand operand = stack[--tos];
        // Я же это удалял...
//        stack[tos] = null;
        return operand;
    }

    public Operand peek() {
        return stack[tos - 1];
    }

    public void dup1_x1() {
        stack[tos] = stack[tos - 1];
        stack[tos - 1] = stack[tos - 2];
        stack[tos - 2] = stack[tos];
        tos++;
    }

    public void dup1_x2() {
        stack[tos] = stack[tos - 1];
        stack[tos - 1] = stack[tos - 2];
        stack[tos - 2] = stack[tos - 3];
        stack[tos - 3] = stack[tos];
        tos++;
    }

    public void dup2_x1() {
        stack[tos + 1] = stack[tos - 1];
        stack[tos] = stack[tos - 2];
        stack[tos - 2] = stack[tos - 3];
        stack[tos - 3] = stack[tos + 1];
        stack[tos - 4] = stack[tos];
        tos += 2;
    }

    public void dup2_x2() {
        stack[tos + 1] = stack[tos - 1];
        stack[tos] = stack[tos - 2];
        stack[tos - 1] = stack[tos - 3];
        stack[tos - 2] = stack[tos - 4];
        stack[tos - 4] = stack[tos + 1];
        stack[tos - 5] = stack[tos];
        tos += 2;
    }

    @Deprecated
    public void duplicate(int count, int x) {
        if (count == 1) {
            for (Operand val = stack[tos - 1]; --x >= 0; )
                stack[tos++] = val;
        } else {
            for (int i = 0; i < x; i++)
                System.arraycopy(stack, (tos - count), stack, (tos + count * i), count);
            tos += count * x;
        }
    }

    @Deprecated
    public void move(int x) {
        Operand temp = stack[tos - 1];
        System.arraycopy(stack, (tos + x - 1), stack, (tos + x), (x < 0) ? -x : x);
        stack[tos + x - 1] = temp;
    }

    public void store(int id, Operand value) {
        locals[id] = value;
    }

    public Operand load(int id) {
        return locals[id];
    }
}
