package jua.compiler;

public final class Module {

    public final Source source;

    public final Executable[] executables;

    public Module(Source source, Executable[] executables) {
        this.source = source;
        this.executables = executables;
    }

    public static class Executable {

        public final String name;
        public final String fileName;
        public final InstructionUtils.InstrNode[] code;
        public final int regSize;
        public final int stackSize;
        public final Object[] constantPool; // todo: replace with a compiler object
        public final LineNumberTable lineNumberTable;
        public final int reqargs, totargs;
        public final Object[] defs;
        public final String[] varnames;
        public final int flags;

        public Executable(String name, String fileName, InstructionUtils.InstrNode[] code,
                          int regSize, int stackSize, Object[] constantPool,
                          LineNumberTable lineNumberTable, int reqargs, int totargs, Object[] defs, String[] varnames, int flags) {
            this.name = name;
            this.fileName = fileName;
            this.code = code;
            this.regSize = regSize;
            this.stackSize = stackSize;
            this.constantPool = constantPool;
            this.lineNumberTable = lineNumberTable;
            this.reqargs = reqargs;
            this.totargs = totargs;
            this.defs = defs;
            this.varnames = varnames;
            this.flags = flags;
        }
    }
}
