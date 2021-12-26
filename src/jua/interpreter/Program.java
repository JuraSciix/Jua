package jua.interpreter;

import jua.interpreter.states.State;

public class Program {

    public static Program empty() {
        // stackSize = 1 because child program always delegate value to him
        return new Program(null, new State[0], new int[]{-1}, 1, 0);
    }

    public static Program coroutine(Environment parent, int argc) {
        // min stackSize value = 1 because child program always delegate value to him
        return new Program(parent.currentFile(), new State[0], new int[]{parent.currentLine()}, Math.max(argc, 1), 0);
    }

    public final String filename;

    public final State[] states;

    public final int[] lines;

    public final int stackSize;

    public final int localsSize;

    public Program(String filename, State[] states, int[] lines, int stackSize, int localsSize) {
        this.filename = filename;
        this.states = states;
        this.lines = lines;
        this.stackSize = stackSize;
        this.localsSize = localsSize;
    }

    public Frame build() {
        return new Frame(filename, states, lines, stackSize, localsSize);
    }
}
