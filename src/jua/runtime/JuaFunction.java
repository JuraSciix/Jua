package jua.runtime;

import jua.interpreter.Program;

public class JuaFunction {

    private final String name;

    private final int minArgc, maxArgc;

    // todo: Классы пакета (модуля) interpreter могут обращаться к runtime, но не наоборот.
    private final Program program;

    public JuaFunction(String name, int minArgc, int maxArgc, Program program) {
        this.name = name;
        this.minArgc = minArgc;
        this.maxArgc = maxArgc;
        this.program = program;
    }

    public String getName() {
        return name;
    }

    public int getMinArgc() {
        return minArgc;
    }

    public int getMaxArgc() {
        return maxArgc;
    }

    public Program getProgram() {
        return program;
    }
}
