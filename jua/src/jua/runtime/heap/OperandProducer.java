package jua.runtime.heap;

import jua.runtime.heap.Operand;

@Deprecated
@FunctionalInterface
public interface OperandProducer<T> {

    Operand get(T value);
}
