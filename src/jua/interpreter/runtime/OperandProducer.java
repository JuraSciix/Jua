package jua.interpreter.runtime;

@Deprecated
@FunctionalInterface
public interface OperandProducer<T> {

    Operand get(T value);
}
