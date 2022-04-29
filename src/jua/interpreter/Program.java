package jua.interpreter;

import jua.interpreter.instructions.Instruction;
import jua.runtime.Operand;

import java.util.Objects;

/**
 *
 */
public final class Program {

    public static final class LineNumberTable {

        private final short[] codePoints;

        private final int[] lineNumbers;

        public LineNumberTable(short[] codePoints, int[] lineNumbers) {
            Objects.requireNonNull(lineNumbers, "line numbers");
            Objects.requireNonNull(codePoints, "code points");
            assert lineNumbers.length == codePoints.length;
            this.lineNumbers = lineNumbers.clone();
            this.codePoints = codePoints.clone();
        }

        public int get(int cp) {
            int l = 0;
            int h = codePoints.length - 1;
            while (l <= h) {
                int m = (l + h) >> 1;
                int cp1 = codePoints[m] & 0xffff;
                if (cp1 == cp) {
                    l = m + 1;
                    break;
                }
                if (cp1 < cp) {
                    l = m + 1;
                } else {
                    h = m - 1;
                }
            }
            return lineNumbers[l - 1];
        }
    }

    private final String sourceName;

    private final Instruction[] code;

    private final LineNumberTable lineNumberTable;

    private final Operand[] constantPool;

    private final int maxStack, maxLocals;

    private final String[] localNames;

    private final int[] optionals;

    public Program(String sourceName, Instruction[] code, LineNumberTable lineNumberTable,
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
