package jua.runtime;

import jua.runtime.code.CodeSegment;

public class JuaFunction {

    private final String name;

    private final int minArgc, maxArgc;

    // todo: Классы пакета (модуля) interpreter могут обращаться к runtime, но не наоборот.
    private final CodeSegment program;

    public JuaFunction(String name, int minArgc, int maxArgc, CodeSegment program) {
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

    public CodeSegment getProgram() {
        return program;
    }
}
