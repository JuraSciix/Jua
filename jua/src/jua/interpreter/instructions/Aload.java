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
    public int run(InterpreterThread env) {
        Operand key = env.popStack();
        Operand map = env.popStack();
        Operand result = map.get(key);
        // todo: В новой версии языка вместо подмены должна происходит ошибка.
        env.pushStack(result == null ? NullOperand.NULL : result);
        return NEXT;
    }
}