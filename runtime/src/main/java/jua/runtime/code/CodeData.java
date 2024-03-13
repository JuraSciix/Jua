package jua.runtime.code;

import jua.runtime.interpreter.instruction.Instruction;

public final class CodeData {

    /** Число слотов на стеке, используемых в коде. */
    private final int stackWide;

    /** Число регистров (локальных переменных), используемых в коде. */
    private final int regNumber;

    /** Имена переменных. */
    private final String[] vars;

    /** Последовательность инструкций. */
    private final Instruction[] code;

    /** Пул констант, используемых в коде. */
    private final ConstantPool constantPool;

    /** Таблица строк. */
    private final LineNumberTable lineNumTable;

    public CodeData(int stackWide, int locals, String[] vars, Instruction[] code, ConstantPool constantPool, LineNumberTable lineNumTable) {
        this.stackWide = stackWide;
        this.regNumber = locals;
        this.vars = vars;
        this.code = code;
        this.constantPool = constantPool;
        this.lineNumTable = lineNumTable;
    }

    public int getStackWide() {
        return stackWide;
    }

    public int getRegNumber() {
        return regNumber;
    }

    public String[] getVars() {
        return vars;
    }

    public Instruction[] getCode() {
        return code;
    }

    public LineNumberTable getLineNumberTable() {
        return lineNumTable;
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }
}
