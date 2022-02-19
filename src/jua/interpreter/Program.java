package jua.interpreter;

import jua.runtime.Operand;
import jua.interpreter.instructions.Instruction;

/**
 * Программа представляет собой фабрику фреймов.
 */
public final class Program {

    public static final class LineNumberTable {

        final short[] bytecodeIndexes;

        final short[] lineNumbers;

        public LineNumberTable(short[] bytecodeIndexes, short[] lineNumbers) {
            this.bytecodeIndexes = bytecodeIndexes;
            this.lineNumbers = lineNumbers;
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
        if ((bci < 0) || (bci >= instructions.length) || lineNumberTable == null)
            return -1;
        // copied from EternalVM
        int low = 0;
        int high = lineNumberTable.bytecodeIndexes.length - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int mid_bci = lineNumberTable.bytecodeIndexes[mid] & 0xffff;

            if (mid_bci <= bci) {
                low = (mid + 1);
            } else {
                high = (mid - 1);
            }
        }
        return lineNumberTable.lineNumbers[low - 1] & 0xffff;
    }

    public ProgramFrame makeFrame() {
        return new ProgramFrame(this);
    }
}
