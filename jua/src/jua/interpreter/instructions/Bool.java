package jua.interpreter.instructions;

import jua.interpreter.InterpreterThread;
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
// todo: Эта инструкция в качестве пролога для инструкций ifeq/ifne не нужна.
//  Вернуть инструкции iftrue и iffalse.
public enum Bool implements Instruction {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("bool");
    }

    @Override
    public int run(InterpreterThread env) {
        // todo:
        env.pushStack(env.popStack().booleanValue());
        return NEXT;
    }
}