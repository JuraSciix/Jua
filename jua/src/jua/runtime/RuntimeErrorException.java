package jua.runtime;

import jua.interpreter.InterpreterRuntime;

import java.util.Objects;

/**
 * Выбрасывается при возникновении ошибки выполнения.
 */
public class RuntimeErrorException extends RuntimeException {

    public InterpreterRuntime runtime; // todo:  Исправить этот костыль

    public RuntimeErrorException(String message) {
        super(Objects.requireNonNull(message, "message"));
    }
}
