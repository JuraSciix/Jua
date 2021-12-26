package jua.interpreter.lang;

@FunctionalInterface
public interface OperandFunction<T> {

    Operand apply(T value);
}
