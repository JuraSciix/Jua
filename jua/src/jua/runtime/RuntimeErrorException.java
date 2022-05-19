package jua.runtime;

import jua.interpreter.InterpreterThread;

import java.util.Objects;

/**
 * Выбрасывается при возникновении ошибки выполнения.
 */
public class RuntimeErrorException extends RuntimeException {

    public InterpreterThread runtime; // todo:  Исправить этот костыль

    public RuntimeErrorException(String message) {
        super(Objects.requireNonNull(message, "message"));
    }
}
