package jua.runtime.heap;

@Deprecated
@FunctionalInterface
public interface OperandProducer<T> {

    Operand get(T value);
}
