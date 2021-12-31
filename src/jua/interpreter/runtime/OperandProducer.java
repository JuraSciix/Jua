package jua.interpreter.runtime;

@FunctionalInterface
public interface OperandProducer<T> {

    Operand get(T value);
}
