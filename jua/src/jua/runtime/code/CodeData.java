package jua.runtime.code;

import jua.interpreter.instruction.Instruction;

import java.util.Objects;

public final class CodeData {

    /** Число слотов на стеке, используемых в коде. */
    public final int stackSize;

    /** Число регистров (локальных переменных), используемых в коде. */
    public final int registers;

    /** Последовательность инструкций. */
    public final Instruction[] code;

    /** Пул констант, используемых в коде. */
    public final ConstantPool constantPool;

    /** Таблица строк. */
    public final LineNumberTable lineNumTable;

    public CodeData(int stackSize, int registers, Instruction[] code, ConstantPool constantPool, LineNumberTable lineNumTable) {
        validateFields(stackSize, registers, code, constantPool, lineNumTable);
        this.stackSize = stackSize;
        this.registers = registers;
        this.code = code;
        this.constantPool = constantPool;
        this.lineNumTable = lineNumTable;
    }

    public static void validateFields(int stackSize, int registers, Instruction[] code, ConstantPool constantPool, LineNumberTable lineNumTable) {
        if (stackSize < 0 || stackSize > 0xFFFF) {
            throw new IllegalArgumentException("stack size must be in the range from 0 to 65535: " + stackSize);
        }
        if (registers < 0 || registers > 0xFFFF) {
            throw new IllegalArgumentException("the number of registers must be in the range from 0 to 65535: " + registers);
        }
        if (code == null) {
            throw new IllegalArgumentException("instructions array (code) must not be null");
        }
        if (code.length == 0) {
            throw new IllegalArgumentException("instructions array (code) must not be empty");
        }
        if (constantPool == null) {
            throw new IllegalArgumentException("constant pool must not be empty");
        }
        if (lineNumTable == null) {
            throw new IllegalArgumentException("line number table must not be null");
        }
    }
}
