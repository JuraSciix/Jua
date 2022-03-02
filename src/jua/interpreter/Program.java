package jua.interpreter;

import jua.interpreter.instructions.Instruction;
import jua.runtime.Operand;

/**
 * Программа представляет собой фабрику фреймов.
 */
public final class Program {

    public static final class LineNumberTable {

        final short[] bytecodeIndexes;

        final short[] lineNumbers; // todo: int[]

        public LineNumberTable(short[] bytecodeIndexes, short[] lineNumbers) {
            this.bytecodeIndexes = bytecodeIndexes;
            this.lineNumbers = lineNumbers;
        }

        int get(final int bcp) {
            int l = 0;
            int r = bytecodeIndexes.length - 1;
            if (r < 0) return -1;

            int minIndex = -1;
            int minValue = -1;
            do {
                int i = (l+r)>>>2;
                int j = bytecodeIndexes[i]&0xffff;

                if (j == bcp) return lineNumbers[i]&0xffff;
                else if (bcp < j) r = i - 1;
                else l = i + 1;

                if (j < bcp && j > minValue) {
                    minValue = j;
                    minIndex = i;
                }
            } while (l <= r);

            if (minIndex < 0) return -1;

            return lineNumbers[minIndex]&0xffff;
        }
    }

    public static Program createMain() {
        // stackSize = 1 because child program always delegate value to him
        // ^^^ Я не понимаю что это значит, но трогать пока лучше не буду
        return new Program("main", new Instruction[0],
                createEmptyLineTable(1), 1, 0, new String[0], new Operand[0]);
    }

    public static Program coroutine(InterpreterRuntime parent, int argc) {
        // min stackSize value = 1 because child program always delegate value to him
        // ^^^ Я не понимаю что это значит, но трогать пока лучше не буду
        return new Program(parent.currentFile(), new Instruction[0],
                createEmptyLineTable(parent.currentLine()), Math.max(argc, 1), 0, new String[0],
                new Operand[0]);
    }

    private static LineNumberTable createEmptyLineTable(int lineNumber) {
        return new LineNumberTable(new short[]{0}, new short[]{(short) lineNumber});
    }

    public final String filename;

    public final Instruction[] instructions;

    public final LineNumberTable lineNumberTable;

    public final int stackSize;

    public final int localsSize;

    public final String[] localsNames;

    public final Operand[] constantPool;

    public Program(String filename, Instruction[] instructions, LineNumberTable lineNumberTable, int stackSize, int localsSize, String[] localsNames, Operand[] constantPool) {
        this.filename = filename;
        this.instructions = instructions;
        this.lineNumberTable = lineNumberTable;
        this.stackSize = stackSize;
        this.localsSize = localsSize;
        this.localsNames = localsNames;
        this.constantPool = constantPool;
    }

    public int getInstructionLine(int bci) {
        return lineNumberTable != null ? lineNumberTable.get(bci) : -1;
    }

    public ProgramFrame makeFrame() {
        return new ProgramFrame(this);
    }
}
