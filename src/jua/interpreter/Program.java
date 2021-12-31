package jua.interpreter;

import jua.interpreter.runtime.Operand;
import jua.interpreter.opcodes.Opcode;

/**
 * Программа представляет собой фабрику фреймов.
 */
public final class Program {

    public static final class LineTableEntry {

        public final int startBci;

        public final int lineNumber;

        public LineTableEntry(int startBci, int lineNumber) {
            this.startBci = startBci;
            this.lineNumber = lineNumber;
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

    private static LineTableEntry[] createEmptyLineTable(int lineNumber) {
        return new LineTableEntry[] {
                new LineTableEntry(0, lineNumber)
        };
    }

    public final String filename;

    public final Opcode[] opcodes;

    public final LineTableEntry[] lineTable;

    public final int stackSize;

    public final int localsSize;

    public final String[] localsNames;

    public final Operand[] constantPool;

    public Program(String filename, Opcode[] opcodes, LineTableEntry[] lineTable, int stackSize, int localsSize, String[] localsNames, Operand[] constantPool) {
        this.filename = filename;
        this.opcodes = opcodes;
        this.lineTable = lineTable;
        this.stackSize = stackSize;
        this.localsSize = localsSize;
        this.localsNames = localsNames;
        this.constantPool = constantPool;
    }

    public int getInstructionLine(int bci) {
        int l = 1;
        int r = lineTable.length - 1;
        while (l <= r) {
            int c = (l + r) >>> 1;
            int current = lineTable[c].startBci;

            if (current < bci) {
                l = c + 1;
            } else if (current > bci) {
                r = c - 1;
            } else {
                return lineTable[c].lineNumber;
            }
        }
        return lineTable[l - 1].lineNumber;
    }

    public ProgramFrame makeFrame() {
        return new ProgramFrame(this);
    }
}
