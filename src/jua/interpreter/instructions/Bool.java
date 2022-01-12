package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.compiler.CodePrinter;

/**
 * Эта инструкция используется в качестве пролога для инструкций
 * {@link Ifeq} и {@link Ifne} чтобы работать с нелогическими значениями.
 * <p>
 * Пример:
 *
 * <pre>{@code if [] {
 *     println('.');
 * }}</pre>
 *
 * <pre>{@code
 * 0. newarray
 * 2. bool
 * 3. ifne     #7
 * 4. push     string "."
 * 5. call     println, 1
 * 6. pop
 * 7. halt}</pre>
 */
public enum Bool implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("bool");
    }

    @Override
    public int run(InterpreterRuntime env) {
        // todo:
        env.pushStack(env.popStack().booleanValue());
        return NEXT;
    }
}