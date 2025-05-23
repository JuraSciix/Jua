package jua.runtime.interpreter;

import java.util.Objects;

/**
 * Выбрасывается при возникновении фатальной ошибки в работе интерпретатора.
 * При обработке должен генерироваться дамп с информацией о состоянии интерпретатора в
 * момент возникновения ошибки.
 */
public class InterpreterException extends RuntimeException {

    public InterpreterException(String message) {
        super(Objects.requireNonNull(message, "message"), null, false, false);
    }

    public InterpreterException(String message, Throwable cause) {
        super(Objects.requireNonNull(message, "message"), cause, false, false);
    }

    public InterpreterException(Throwable cause) {
        super(cause);
    }
}
