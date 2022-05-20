package jua.interpreter.instructions;

import jua.interpreter.InterpreterState;
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
    public int run(InterpreterState state) {
        Operand key = state.popStack();
        Operand map = state.popStack();
        Operand result = map.get(key);
        // todo: В новой версии языка вместо подмены должна происходит ошибка.
        state.pushStack(result == null ? NullOperand.NULL : result);
        return NEXT;
    }
}