package jua.compiler;

import jua.runtime.ConstantMemory;

public final class Module {

    public final Source source;

    public final Executable[] executables;

    public final ConstantMemory[] constants;

    public String[] functionNames;

    public Module(Source source, Executable[] executables, ConstantMemory[] constants) {
        this.source = source;
        this.executables = executables;
        this.constants = constants;
    }
}
