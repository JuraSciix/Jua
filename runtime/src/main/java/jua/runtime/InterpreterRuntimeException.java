package jua.runtime;

/**
 * Выбрасывается при возникновении ошибки выполнения кода <b>Jua</b>.
 * <p>
 * Этот класс не поддерживает сжатие и не собирает трассировку стека,
 * потому что является <i>штатным исключением</i>.
 */
public class InterpreterRuntimeException extends RuntimeException {

    public InterpreterRuntimeException(String message) {
        super(message, null, false, false);
    }
}
