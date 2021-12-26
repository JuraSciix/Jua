package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

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
public enum Bool implements State {

    INSTANCE;

    @Override
    public void print(CodePrinter printer) {
        printer.printName("bool");
    }

    @Override
    public int run(Environment env) {
        // todo:
        env.pushStack(env.popStack().booleanValue());
        return NEXT;
    }
}