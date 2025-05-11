package jua.runtime;

import jua.runtime.interpreter.InterpreterThread;

import java.util.Objects;

/**
 * Выбрасывается при возникновении ошибки выполнения.
 */
@Deprecated
public class RuntimeErrorException extends RuntimeException {

    public InterpreterThread thread; // todo:  Исправить этот костыль

    public RuntimeErrorException(String message) {
        super(Objects.requireNonNull(message, "message"), null, false, false);
    }
}
