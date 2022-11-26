package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.InterpreterState;

public class Print implements Instruction {

    protected final int count;

    public Print(int count) {
        this.count = count;
    }

    @Override
    public int stackAdjustment() { return -count; }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("print");
        printer.print(count);
    }

    @Override
    public void run(InterpreterState state) {
        String[] pieces = new String[count];
        for (int i = 1; i <= count; i++) {
            pieces[count - i] = state.popStack().stringVal().toString();

            if (state.thread().isError()) {
                // Прерываем выполнение инструкции, если произошла ошибка.
                return;
            }
        }

        for (int i = 0; i < count; i++) {
            System.out.print(pieces[i]);
        }
        state.next();
    }
}