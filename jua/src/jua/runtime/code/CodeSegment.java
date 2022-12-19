package jua.runtime.code;

import jua.interpreter.instruction.Instruction;

public final class CodeSegment {

    private final Instruction[] code;

    private final int maxStack;

    private final int maxLocals;

    private final ConstantPool constantPool;

    private final LineNumberTable lineNumberTable;

    private final LocalTable localTable;

    public CodeSegment(Instruction[] code, int maxStack, int maxLocals, ConstantPool constantPool, LineNumberTable lineNumberTable, LocalTable localTable) {
        this.code = code;
        this.maxStack = maxStack;
        this.maxLocals = maxLocals;
        this.constantPool = constantPool;
        this.lineNumberTable = lineNumberTable;
        this.localTable = localTable;
    }

    public Instruction[] code()              { return code; }
    public int maxStack()                    { return maxStack; }
    public int maxLocals()                   { return maxLocals; }
    public ConstantPool constantPool()       { return constantPool; }
    public LineNumberTable lineNumberTable() { return lineNumberTable; }
    public LocalTable localTable()           { return localTable; }
}
