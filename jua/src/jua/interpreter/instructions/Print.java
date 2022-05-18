package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.compiler.CodePrinter;

public class Print implements Instruction {

    protected final int count;

    public Print(int count) {
        this.count = count;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.printName("print");
        printer.print(count);
    }

    @Override
    public int run(InterpreterThread env) {
        String[] pieces = new String[count];
        for (int i = 1; i <= count; i++) {
            pieces[count - i] = env.popStack().stringValue();
        }
        for (int i = 0; i < count; i++) {
            System.out.print(pieces[i]);
        }
        return NEXT;
    }
}