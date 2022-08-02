package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.compiler.Tree;
import jua.interpreter.InterpreterState;

public class Getconst implements Instruction {

    private final int id;

    private final Tree.Name name; // todo: Исправить костыль. Нужно чтобы существование константы определялось на этапе компиляции

    public Getconst(int id, Tree.Name name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("getconst");
        printer.print(id);
    }

    @Override
    public int run(InterpreterState state) {
        if (id == -1) {
            state.thread().error("Constant named '%s' does not exists", name.value);
            return ERROR;
        }
        state.pushStack(state.getConstantById(id));
        return NEXT;
    }
}