package jua.interpreter;

import jua.interpreter.runtime.Operand;
import jua.interpreter.opcodes.Opcode;

import java.util.Arrays;

/**
 * Программа представляет собой фабрику фреймов.
 */
public final class Program {

    public static final class LineTable {

        final int[] bcindexes;

        final int[] lineNumbers;

        public LineTable(int[] bcindexes, int[] lineNumbers) {
            this.bcindexes = bcindexes;
            this.lineNumbers = lineNumbers;
        }
    }

    public static Program createMain() {
        // stackSize = 1 because child program always delegate value to him
        // ^^^ Я не понимаю что это значит, но трогать пока лучше не буду
        return new Program("main", new Opcode[0],
                createEmptyLineTable(1), 1, 0, new String[0], new Operand[0]);
    }

    public static Program coroutine(InterpreterRuntime parent, int argc) {
        // min stackSize value = 1 because child program always delegate value to him
        // ^^^ Я не понимаю что это значит, но трогать пока лучше не буду
        return new Program(parent.currentFile(), new Opcode[0],
                createEmptyLineTable(parent.currentLine()), Math.max(argc, 1), 0, new String[0],
                new Operand[0]);
    }

    private static LineTable createEmptyLineTable(int lineNumber) {
        return new LineTable(new int[]{0}, new int[]{lineNumber});
    }

    public final String filename;

    public final Opcode[] opcodes;

    public final LineTable lineTable;

    public final int stackSize;

    public final int localsSize;

    public final String[] localsNames;

    public final Operand[] constantPool;

    public Program(String filename, Opcode[] opcodes, LineTable lineTable, int stackSize, int localsSize, String[] localsNames, Operand[] constantPool) {
        this.filename = filename;
        this.opcodes = opcodes;
        this.lineTable = lineTable;
        this.stackSize = stackSize;
        this.localsSize = localsSize;
        this.localsNames = localsNames;
        this.constantPool = constantPool;
    }

    public int getInstructionLine(int bci) {
        if ((bci < 0) || (bci > opcodes.length) || lineTable == null)
            return -1;
        int a = Arrays.binarySearch(lineTable.bcindexes, bci);
        if (a < 0)
            a = ~a - 1;
        return lineTable.lineNumbers[a];
    }

    public ProgramFrame makeFrame() {
        return new ProgramFrame(this);
    }
}
