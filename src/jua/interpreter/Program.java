package jua.interpreter;

import jua.interpreter.states.State;

/**
 * Программа представляет собой фабрику фреймов.
 */
public final class Program {

    public static final class LineTableEntry {

        public final int lineNumber;

        public final int startIp;

        public LineTableEntry(int lineNumber, int startIp) {
            this.lineNumber = lineNumber;
            this.startIp = startIp;
        }
    }

    public static Program createMain() {
        // stackSize = 1 because child program always delegate value to him
        // ^^^ Я не понимаю что это значит, но трогать пока лучше не буду
        return new Program("main", new State[0],
                createEmptyLineTable(1), 1, 0, new String[0]);
    }

    public static Program coroutine(Environment parent, int argc) {
        // min stackSize value = 1 because child program always delegate value to him
        // ^^^ Я не понимаю что это значит, но трогать пока лучше не буду
        return new Program(parent.currentFile(), new State[0],
                createEmptyLineTable(parent.currentLine()), Math.max(argc, 1), 0, new String[0]);
    }

    private static LineTableEntry[] createEmptyLineTable(int lineNumber) {
        return new LineTableEntry[] {
                new LineTableEntry(lineNumber, 0)
        };
    }

    public final String filename;

    public final State[] states;

    public final LineTableEntry[] lineTable;

    public final int stackSize;

    public final int localsSize;

    public final String[] localsNames;

    public Program(String filename, State[] states, LineTableEntry[] lineTable, int stackSize, int localsSize, String[] localsNames) {
        this.filename = filename;
        this.states = states;
        this.lineTable = lineTable;
        this.stackSize = stackSize;
        this.localsSize = localsSize;
        this.localsNames = localsNames;
    }

    public int getInstructionLine(int ip) {
        int l = 1;
        int r = lineTable.length - 1;
        while (l <= r) {
            int c = (l + r) >>> 1;
            int current = lineTable[c].startIp;

            if (current < ip) {
                l = c + 1;
            } else if (current > ip) {
                r = c - 1;
            } else {
                return lineTable[c].lineNumber;
            }
        }
        return lineTable[l - 1].lineNumber;
    }

    public Frame makeFrame() {
        return new Frame(this);
    }
}
