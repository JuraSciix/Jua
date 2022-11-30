package jua.interpreter.instruction;

import jua.compiler.CodePrinter;
import jua.interpreter.Address;
import jua.interpreter.InterpreterState;

@Deprecated
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

    protected boolean interrupted = false;

    @Override
    public void run(InterpreterState state) {
        String[] pieces = new String[count];
        Address tmp = state.getTemporalAddress();
        for (int i = 1; i <= count; i++) {
            if (!state.popStack().stringVal(tmp)) {
                // Прерываем выполнение инструкции, если произошла ошибка.
                interrupted = true;
                return;
            }
            pieces[count - i] = tmp.toString();
        }
        tmp.reset();

        for (int i = 0; i < count; i++) {
            System.out.print(pieces[i]);
        }
        state.next();
    }
}