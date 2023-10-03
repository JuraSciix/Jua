package jua.runtime.code;

import jua.runtime.interpreter.instruction.Instruction;

public final class CodeData {

    /** Число слотов на стеке, используемых в коде. */
    public final int stack;

    /** Число регистров (локальных переменных), используемых в коде. */
    public final int locals;

    /** Имена переменных. */
    public final String[] localNames;

    /** Последовательность инструкций. */
    public final Instruction[] code;

    /** Пул констант, используемых в коде. */
    public final ConstantPool constantPool;

    /** Таблица строк. */
    public final LineNumberTable lineNumTable;

    public CodeData(int stack, int locals, String[] localNames, Instruction[] code, ConstantPool constantPool, LineNumberTable lineNumTable) {
        this.stack = stack;
        this.locals = locals;
        this.localNames = localNames;
        this.code = code;
        this.constantPool = constantPool;
        this.lineNumTable = lineNumTable;
    }

    public ConstantPool constantPool() {
        return constantPool;
    }
}
