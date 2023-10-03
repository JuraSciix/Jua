package jua.compiler;

import jua.compiler.InstructionUtils.InstrNode;
import jua.runtime.code.ConstantPool;
import jua.runtime.code.LineNumberTable;

public class Executable {

    public final String name;
    public final String fileName;
    public final InstrNode[] code;
    public final int regSize;
    public final int stackSize;
    public final ConstantPool constantPool; // todo: replace with compiler object
    public final LineNumberTable lineNumberTable;
    public final int reqargs, totargs;
    public final Object[] defs;
    public final String[] varnames;

    public Executable(String name, String fileName, InstrNode[] code, int regSize, int stackSize, ConstantPool constantPool, LineNumberTable lineNumberTable, int reqargs, int totargs, Object[] defs, String[] varnames) {
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
    }
}
