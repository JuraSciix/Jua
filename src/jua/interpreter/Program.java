package jua.interpreter;

import jua.interpreter.lang.Operand;
import jua.interpreter.states.State;

public class Program {

    public static class Builder {

        public static Builder empty() {
            // stackSize = 1 because child program always delegate value to him
            return new Builder(null, new State[0], new int[]{-1}, 1, 0);
        }

        public static Builder coroutine(Environment parent, int argc) {
            // min stackSize value = 1 because child program always delegate value to him
            return new Builder(parent.currentFile(), new State[0], new int[]{parent.currentLine()}, Math.max(argc, 1), 0);
        }

        public final String filename;

        public final State[] states;

        public final int[] lines;

        public final int stackSize;

        public final int localsSize;

        public Builder(String filename, State[] states, int[] lines, int stackSize, int localsSize) {
            this.filename = filename;
            this.states = states;
            this.lines = lines;
            this.stackSize = stackSize;
            this.localsSize = localsSize;
        }

        public Program build() {
            return new Program(filename, states, lines, stackSize, localsSize);
        }
    }

    private final String filename;

    private final State[] states;

    private final int[] lines;

    private Operand[] stack;

    private Operand[] locals;

    private int pc = 0;

    private int st = 0;

    private Program(String filename, State[] states, int[] lines, int stackSize, int localsSize) {
        this.filename = filename;
        this.states = states;
        this.lines = lines;

        stack = new Operand[stackSize];
        locals = new Operand[localsSize];
    }

    void incPC() { // for env
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

    void run(Environment env) { // for env
        states[pc].run(env);
    }

    void clearStack() { // for env
        stack = null;
    }

    void clearLocals() { // for env
        locals = null;
    }

    public void push(Operand operand) {
        stack[st++] = operand;
    }

    public Operand pop() {
        Operand operand = stack[--st];
        stack[st] = null;
        return operand;
    }

    public Operand peek() {
        return stack[st - 1];
    }
    
    public void duplicate(int count, int x) {
        if (count == 1) {
            for (Operand val = stack[st - 1]; --x >= 0; )
                stack[st++] = val;
        } else {
            for (int i = 0; i < x; i++)
                System.arraycopy(stack, (st - count), stack, (st + count * i), count);
            st += count * x;
        }
    }

    public void move(int x) {
        Operand temp = stack[st - 1];
        System.arraycopy(stack, (st + x - 1), stack, (st + x), (x < 0) ? -x : x);
        stack[st + x - 1] = temp;
    }

    public void store(int id, Operand value) {
        locals[id] = value;
    }

    public Operand load(int id) {
        return locals[id];
    }
}
