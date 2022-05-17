package jua.runtime.code;

import jua.interpreter.instructions.Instruction;
import jua.runtime.Operand;

/**
 *
 */
public final class CodeSegment {

    private final String sourceName;

    private final Instruction[] code;

    private final LineNumberTable lineNumberTable;

    private final Operand[] constantPool;

    private final int maxStack, maxLocals;

    private final String[] localNames;

    private final int[] optionals;

    public CodeSegment(String sourceName, Instruction[] code, LineNumberTable lineNumberTable,
                       Operand[] constantPool, int maxStack, int maxLocals, String[] localNames, int[] optionals) {
        this.sourceName = sourceName;
        this.code = code;
        this.lineNumberTable = lineNumberTable;
        this.constantPool = constantPool;
        this.maxStack = maxStack;
        this.maxLocals = maxLocals;
        this.localNames = localNames;
        this.optionals = optionals;
    }

    public String getSourceName() {
        return sourceName;
    }

    public Instruction[] getCode() {
        return code;
    }

    public LineNumberTable getLineNumberTable() {
        return lineNumberTable;
    }

    public Operand[] getConstantPool() {
        return constantPool;
    }

    public int getMaxStack() {
        return maxStack;
    }

    public int getMaxLocals() {
        return maxLocals;
    }

    public String[] getLocalNames() {
        return localNames;
    }

    public int[] getOptionals() {
        return optionals;
    }
}
