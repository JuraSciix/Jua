package jua.interpreter;

import jua.interpreter.lang.Operand;
import jua.interpreter.states.State;

public class Frame {

    // todo: ну... исправить
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

    private final String filename;

    private final State[] states;

    // todo: Хранить только начальные позиции каждой строки ассоциированные с индексом первой операции
    private final int[] lines;

    private Operand[] stack;

    private Operand[] locals;

    private int pc = -1;

    private int tos = 0;

    Frame(String filename, State[] states, int[] lines, int stackSize, int localsSize) {
        this.filename = filename;
        this.states = states;
        this.lines = lines;

        stack = new Operand[stackSize];
        locals = new Operand[localsSize];
    }

    public void incPC() {
        pc++;
    }

    void setPC(int newPC) { // for env
        pc = newPC;
    }

    boolean next() { // for env
        return pc < states.length;
    }

    String filename() { // for env
        return filename;
    }

    int line() { // for env
        return lines[pc];
    }

    void run(Environment env) {
        State[] states = this.states;
        int bci = pc;
        try {
            while (true) {
                //System.out.printf("[%d] %s%n", bci, states[bci].getClass().getName());
                bci += states[bci].run(env);
            }
        } finally {
            pc = bci;
        }
    }

    void clearStack() { // for env
        stack = null;
    }

    void clearLocals() { // for env
        locals = null;
    }

    public void push(Operand operand) {
        stack[tos++] = operand;
    }

    public Operand pop() {
        Operand operand = stack[--tos];
        stack[tos] = null;
        return operand;
    }

    public Operand peek() {
        return stack[tos - 1];
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
