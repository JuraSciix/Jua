package jua.interpreter.instruction;

public interface CodePrinter {

    void printName(String name);

    void printFunctionRef(int fr);

    void print(Object o);

    void printConstRef(int cr);

    void printIp(int ip);

    void printLocal(int id);

    void printCase(int[] values, int cp);

    void printLiteral(int id);
}
