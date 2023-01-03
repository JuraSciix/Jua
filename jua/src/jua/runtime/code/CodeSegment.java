package jua.runtime.code;

import jua.interpreter.instruction.Instruction;

import java.util.Objects;

public final class CodeSegment {

    private final Instruction[] code;

    private final int maxStack;

    private final int maxLocals;

    private final ConstantPool constantPool;

    private final LineNumberTable lineNumberTable;

    private final LocalTable localTable;

    public CodeSegment(Instruction[] code, int maxStack, int maxLocals, ConstantPool constantPool, LineNumberTable lineNumberTable, LocalTable localTable) {
        this.code = Objects.requireNonNull(code);
        this.maxStack = maxStack & 0xffff;
        this.maxLocals = maxLocals & 0xfffff;
        this.constantPool = Objects.requireNonNull(constantPool);
        this.lineNumberTable = Objects.requireNonNull(lineNumberTable);
        this.localTable = Objects.requireNonNull(localTable);
    }

    public Instruction[] code()              { return code; }
    public int maxStack()                    { return maxStack & 0xffff; }
    public int maxLocals()                   { return maxLocals & 0xffff; }
    public ConstantPool constantPool()       { return constantPool; }
    public LineNumberTable lineNumberTable() { return lineNumberTable; }
    public LocalTable localTable()           { return localTable; }
}
