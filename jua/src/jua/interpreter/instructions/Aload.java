package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
import jua.runtime.NullOperand;
import jua.runtime.Operand;
import jua.compiler.CodePrinter;

public enum Aload implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("aload");
    }

    @Override
    public int run(InterpreterThread thread) {
        Operand key = thread.popStack();
        Operand map = thread.popStack();
        Operand result = map.get(key);
        // todo: В новой версии языка вместо подмены должна происходит ошибка.
        thread.pushStack(result == null ? NullOperand.NULL : result);
        return NEXT;
    }
}