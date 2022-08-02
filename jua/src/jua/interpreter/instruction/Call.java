package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public final class Call implements Instruction {

    private final int id;
    private final int argc;
    private final String name; // todo: Исправить костыль. Нужно чтобы существование функции определялось на этапе компиляции

    public Call(int id, int argc, String name) {
        this.id = id;
        this.argc = argc;
        this.name = name;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("call");
        printer.print(id);
        printer.print(argc);
    }

    @Override
    public int run(InterpreterState state) {
        // todo: Исправить костыль с вызовом несуществующей функции
        if (id == -1) {
            // Функции не существует.
            state.thread().error("Function named '%s' does not exists", name);
            return ERROR;
        }
        state.set_cp_advance(1);
        state.thread().set_callee(id, argc);
        return 0; // I'll be back...
    }
}