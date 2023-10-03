package jua.runtime.interpreter.instruction;

public interface InstructionPrinter {

    /**
     * Печатает название инструкции.
     */
    void printName(String name);

    void printFuncRef(int fr);

    void printConstRef(int cr);

    /**
     * Печатает значение типа счетчика программы.
     */
    void printCp(int pc);

    /**
     * Печатает номер переменной.
     */
    void printLocal(int id);

    void printLiteral(int id);

    void beginSwitch();

    void endSwitch();

    void printCase(int[] agentLiterals, int casePC);

    /**
     * Печатает произвольный объект.
     */
    @Deprecated
    void print(Object o);

    /***
     * Планирует восстановление tos в определенной точке программы.
     */
    void restoreTosIn(int pc);
}
